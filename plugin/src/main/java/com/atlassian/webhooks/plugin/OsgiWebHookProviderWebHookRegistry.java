package com.atlassian.webhooks.plugin;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.plugin.impl.WebHookRegistrarImpl;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterables.*;

public final class OsgiWebHookProviderWebHookRegistry implements WebHookRegistry
{
    private final Map<String, WebHookRegistration> registrationsByKey;
    private final SetMultimap<Class<?>, WebHookRegistration> registrationsByEvent;
    private final Map<WebHookProvider, Set<WebHookRegistration>> registrationsByProvider;

    public OsgiWebHookProviderWebHookRegistry(WaitableServiceTrackerFactory factory)
    {
        this.registrationsByEvent = Multimaps.synchronizedSetMultimap(HashMultimap.<Class<?>, WebHookRegistration>create());
        this.registrationsByKey = new ConcurrentHashMap<String, WebHookRegistration>();
        this.registrationsByProvider = new ConcurrentHashMap<WebHookProvider, Set<WebHookRegistration>>();

        checkNotNull(factory).create(WebHookProvider.class, new WebHookProviderWaitableServiceTrackerCustomizer());
    }

    @Override
    public Iterable<String> getWebHookIds()
    {
        return registrationsByKey.keySet();
    }

    @Override
    public Iterable<WebHookEvent> getWebHooks(final Object event)
    {
        return transform(registrationsByEvent.get(event.getClass()), new Function<WebHookRegistration, WebHookEvent>()
        {
            @Override
            public WebHookEvent apply(final WebHookRegistration registration)
            {
                return new WebHookEventImpl(registration.getId(), event, registration.getEventMatcher(),
                        Suppliers.memoize(new Supplier<String>()
                        {
                            @Override
                            public String get()
                            {
                                return registration.getEventSerializer(event).getJson();
                            }
                        }));
            }
        });
    }

    private static final class WebHookEventImpl implements WebHookEvent
    {
        private final String id;
        private final Object event;
        private final EventMatcher eventMatcher;
        private final Supplier<String> body;

        public WebHookEventImpl(String id, Object event, EventMatcher eventMatcher, Supplier
                <String> body)
        {
            this.id = id;
            this.eventMatcher = eventMatcher;
            this.event = event;
            this.body = body;
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public Object getEvent()
        {
            return event;
        }

        @Override
        public EventMatcher<Object> getEventMatcher()
        {
            return eventMatcher;
        }

        @Override
        public String getJson()
        {
            return body.get();
        }
    }

    private final class WebHookProviderWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookProvider>
    {
        @Override
        public WebHookProvider adding(WebHookProvider service)
        {
            final WebHookRegistrarImpl registrar = new WebHookRegistrarImpl();
            service.provide(registrar);

            for (WebHookRegistration reg : registrar.getRegistrations())
            {
                if (reg.getEventClass() != null)
                {
                    registrationsByEvent.put(reg.getEventClass(), reg);
                }
                registrationsByKey.put(reg.getId(), reg);
            }
            registrationsByProvider.put(service, registrar.getRegistrations());

            return service;
        }

        @Override
        public void removed(WebHookProvider service)
        {
            Set<WebHookRegistration> registrations = registrationsByProvider.remove(service);
            for (Iterator<WebHookRegistration> i = registrationsByEvent.values().iterator(); i.hasNext(); )
            {
                if (registrations.contains(i.next()))
                {
                    i.remove();
                }
            }

            for (Iterator<WebHookRegistration> i = registrationsByKey.values().iterator(); i.hasNext(); )
            {
                if (registrations.contains(i.next()))
                {
                    i.remove();
                }
            }
        }
    }
}
