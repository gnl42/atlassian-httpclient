package com.atlassian.webhooks.plugin;


import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Function;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 * Waits for new services which implements WebHookListenerRegistry.
 */
public class OsgiWebHookListenerProvider implements WebHookListenerProvider
{

    private Set<WebHookListenerRegistry> registries = new HashSet<WebHookListenerRegistry>();

    public OsgiWebHookListenerProvider(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookListenerRegistry.class, new WebHookListenerRegistryWaitableServiceTrackerCustomizer());
    }

    @Override
    public Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent)
    {
        return concat(transform(registries, new Function<WebHookListenerRegistry, Iterable<WebHookListener>>()
        {
            @Override
            public Iterable<WebHookListener> apply(final WebHookListenerRegistry registry)
            {
                return registry.getListeners(webHookEvent);
            }
        }));
    }

    private final class WebHookListenerRegistryWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookListenerRegistry>
    {
        @Override
        public WebHookListenerRegistry adding(final WebHookListenerRegistry service)
        {
            registries.add(service);
            return service;
        }

        @Override
        public void removed(final WebHookListenerRegistry service)
        {
            registries.remove(service);
        }
    }
}
