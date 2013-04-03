package com.atlassian.webhooks.plugin;


import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class DelegatingWebHookConsumerProvider implements WebHookConsumerProvider
{
    private final Iterable<WebHookConsumerProvider> consumerProviders;

    public DelegatingWebHookConsumerProvider(final Iterable<WebHookConsumerProvider> consumerRegistries)
    {
        this.consumerProviders = consumerRegistries;
    }

    @Override
    public Iterable<WebHookConsumer> getConsumers(final WebHookEvent webHookEvent)
    {
        return Iterables.concat(Iterables.transform(consumerProviders, new Function<WebHookConsumerProvider, Iterable<WebHookConsumer>>()
        {
            @Override
            public Iterable<WebHookConsumer> apply(final WebHookConsumerProvider registry)
            {
                return registry.getConsumers(webHookEvent);
            }
        }));
    }
}
