package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Retrieves all WebHook listeners from AO and delegates the transformation to the implementation of {@link WebHookListenerTransformer}
 * found in the bundleContext. Filters disabled listeners and those which couldn't be transformed by the provider.
 */
public class PersistentWebHookListenerProvider implements WebHookListenerProvider
{
    private final WebHookListenerTransformer webHookListenerTransformer;
    private final WebHookListenerService webHookListenerService;

    public PersistentWebHookListenerProvider(WebHookListenerService webHookListenerService,
            WebHookListenerTransformer webHookListenerTransformer)
    {

        this.webHookListenerTransformer = checkNotNull(webHookListenerTransformer);
        this.webHookListenerService = checkNotNull(webHookListenerService);
    }

    @Override
    public Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent)
    {
        return filter(transform(filter(webHookListenerService.getAllWebHookListeners(), new Predicate<WebHookListenerParameters>()
        {
            @Override
            public boolean apply(final WebHookListenerParameters webHookListenerParameters)
            {
                return webHookListenerParameters.isEnabled();
            }
        }), new Function<WebHookListenerParameters, WebHookListener>()
        {
            @Override
            public WebHookListener apply(final WebHookListenerParameters webHookListenerParameters)
            {
                return webHookListenerTransformer.transform(webHookListenerParameters).orNull();
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
