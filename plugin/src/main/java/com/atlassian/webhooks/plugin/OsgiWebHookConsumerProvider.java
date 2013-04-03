package com.atlassian.webhooks.plugin;


import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Function;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class OsgiWebHookConsumerProvider implements WebHookConsumerProvider
{

    private Set<WebHookConsumerRegistry> registries = new HashSet<WebHookConsumerRegistry>();

    public OsgiWebHookConsumerProvider(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookConsumerRegistry.class, new WebHookConsumerRegistryWaitableServiceTrackerCustomizer());
    }

    @Override
    public Iterable<WebHookConsumer> getConsumers(final WebHookEvent webHookEvent)
    {
        return concat(transform(registries, new Function<WebHookConsumerRegistry, Iterable<WebHookConsumer>>()
        {
            @Override
            public Iterable<WebHookConsumer> apply(final WebHookConsumerRegistry registry)
            {
                return registry.getConsumers(webHookEvent);
            }
        }));
    }

    private final class WebHookConsumerRegistryWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookConsumerRegistry>
    {
        @Override
        public WebHookConsumerRegistry adding(final WebHookConsumerRegistry service)
        {
            registries.add(service);
            return service;
        }

        @Override
        public void removed(final WebHookConsumerRegistry service)
        {
            registries.remove(service);
        }
    }
}
