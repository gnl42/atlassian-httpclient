package com.atlassian.webhooks.plugin;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.plugin.impl.WebHookRegistrarImpl;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

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

        checkNotNull(factory).create(WebHookProvider.class, new FilteringWaitableServiceTrackerCustomizer<WebHookProvider>(new WebHookProviderWaitableServiceTrackerCustomizer()));
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
            public WebHookEvent apply(WebHookRegistration registration)
            {
                return new WebHookEventImpl(registration.getId(), event, registration.getEventMatcher(), registration.getEventSerializer(event).getJson());
            }
        });
    }

    private static final class WebHookEventImpl implements WebHookEvent
    {
        private final String id;
        private final Object event;
        private final EventMatcher eventMatcher;
        private final String body;

        public WebHookEventImpl(String id, Object event, EventMatcher eventMatcher, String body)
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
            return body;
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

    // this class only exists to work around a but in WaitableServiceTracker where #adding can be called more than once
    // if a Spring application context refresh occurs.
    private static final class FilteringWaitableServiceTrackerCustomizer<T> implements WaitableServiceTrackerCustomizer<T>
    {
        private final WaitableServiceTrackerCustomizer<T> delegate;
        private final Set<ServiceId> serviceIds;

        private FilteringWaitableServiceTrackerCustomizer(WaitableServiceTrackerCustomizer<T> delegate)
        {
            this.delegate = checkNotNull(delegate);
            this.serviceIds = new CopyOnWriteArraySet<ServiceId>();
        }

        @Override
        public T adding(T service)
        {
            final ServiceId serviceId = new ServiceId(service);
            if (!serviceIds.contains(serviceId))
            {
                serviceIds.add(serviceId);
                return delegate.adding(service);
            }
            return service;
        }

        @Override
        public void removed(T service)
        {
            serviceIds.remove(new ServiceId(service));
        }
    }

    private static final class ServiceId
    {
        private final String className;
        private final int identityHashCode;

        ServiceId(Object service)
        {
            this.className = checkNotNull(service).getClass().getName();
            this.identityHashCode = System.identityHashCode(service);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(className, identityHashCode);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == null)
            {
                return false;
            }
            if (!(o instanceof ServiceId))
            {
                return false;
            }

            final ServiceId other = (ServiceId) o;

            return Objects.equal(this.className, other.className)
                    && Objects.equal(this.identityHashCode, other.identityHashCode);
        }
    }
}
