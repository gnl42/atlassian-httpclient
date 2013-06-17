package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.plugin.ao.DelegatingWebHookListenerRegistrationParameters;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.service.WebHookListenerService;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PersistentWebHookListenerProvider implements WebHookListenerProvider
{
    private final WebHookListenerService webHookListenerService;
    private final WebHookListenerTransformer webHookListenerTransformer;

    public PersistentWebHookListenerProvider(WebHookListenerService webHookListenerService, WebHookListenerTransformer webHookListenerTransformer)
    {
        this.webHookListenerService = webHookListenerService;
        this.webHookListenerTransformer = webHookListenerTransformer;
    }

    @Override
    public Iterable<WebHookListener> getListeners(WebHookEvent webHookEvent)
    {
        return Iterables.transform(Iterables.filter(webHookListenerService.getAllWebHooks(), new Predicate<WebHookAO>()
        {
            @Override
            public boolean apply(final WebHookAO webHookAO)
            {
                return webHookAO.isEnabled();
            }
        }), new Function<WebHookAO, WebHookListener>()
        {
            @Override
            public WebHookListener apply(final WebHookAO webHookAO)
            {
                return webHookListenerTransformer.transform(new DelegatingWebHookListenerRegistrationParameters(webHookAO));
            }
        });
    }
}
