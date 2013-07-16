package com.atlassian.webhooks.plugin;


import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Concats the listener registered in various WebHookListenerProvider implementationsP:
 *  - module descriptor listener provider
 *  - persistent listener provider
 *  - osgi listener provider
 */
public class DelegatingWebHookListenerProvider implements WebHookListenerProvider
{
    private final Iterable<WebHookListenerProvider> listenerProviders;

    public DelegatingWebHookListenerProvider(final Iterable<WebHookListenerProvider> listenerRegistries)
    {
        this.listenerProviders = listenerRegistries;
    }

    @Override
    public Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent)
    {
        return Iterables.concat(Iterables.transform(listenerProviders, new Function<WebHookListenerProvider, Iterable<WebHookListener>>()
        {
            @Override
            public Iterable<WebHookListener> apply(final WebHookListenerProvider registry)
            {
                return registry.getListeners(webHookEvent);
            }
        }));
    }
}
