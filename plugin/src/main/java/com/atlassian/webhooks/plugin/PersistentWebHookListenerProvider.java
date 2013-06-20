package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.plugin.ao.DelegatingWebHookListenerParameters;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.service.InternalWebHookListenerService;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Retrieves all WebHook listeners from AO and delegates the transformation to the implementation of {@link WebHookListenerTransformer}
 * found in the bundleContext. Filters disabled listeners and those which couldn't be transformed by the provider.
 */
public class PersistentWebHookListenerProvider implements WebHookListenerProvider
{
    private final InternalWebHookListenerService internalWebHookListenerService;
    private final WebHookListenerTransformer webHookListenerTransformer;

    public PersistentWebHookListenerProvider(InternalWebHookListenerService internalWebHookListenerService, WebHookListenerTransformer webHookListenerTransformer)
    {
        this.internalWebHookListenerService = internalWebHookListenerService;
        this.webHookListenerTransformer = webHookListenerTransformer;
    }

    @Override
    public Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent)
    {
        return filter(transform(filter(internalWebHookListenerService.getAllWebHookListeners(), new Predicate<WebHookAO>()
        {
            @Override
            public boolean apply(final WebHookAO webHookListenerParameters)
            {
                return webHookListenerParameters.isEnabled();
            }
        }), new Function<WebHookAO, WebHookListener>()
        {
            @Override
            public WebHookListener apply(final WebHookAO webHookAO)
            {
                return webHookListenerTransformer.transform(new DelegatingWebHookListenerParameters(webHookAO)).orNull();
            }
        }), new Predicate<WebHookListener>()
        {
            @Override
            public boolean apply(WebHookListener webHookListener)
            {
                return webHookListener != null;
            }
        });
    }
}
