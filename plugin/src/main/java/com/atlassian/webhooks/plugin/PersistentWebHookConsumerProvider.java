package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.plugin.ao.DelegatingWebHookRegistrationParameters;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.service.WebHookConsumerService;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookModelTransformer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PersistentWebHookConsumerProvider implements WebHookConsumerProvider
{
    private final WebHookConsumerService webHookConsumerService;
    private final WebHookModelTransformer webHookModelTransformer;

    public PersistentWebHookConsumerProvider(WebHookConsumerService webHookConsumerService, WebHookModelTransformer webHookModelTransformer)
    {
        this.webHookConsumerService = webHookConsumerService;
        this.webHookModelTransformer = webHookModelTransformer;
    }

    @Override
    public Iterable<WebHookConsumer> getConsumers(WebHookEvent webHookEvent)
    {
        return Iterables.transform(Iterables.filter(webHookConsumerService.getAllWebHooks(), new Predicate<WebHookAO>()
        {
            @Override
            public boolean apply(final WebHookAO webHookAO)
            {
                return webHookAO.isEnabled();
            }
        }), new Function<WebHookAO, WebHookConsumer>()
        {
            @Override
            public WebHookConsumer apply(final WebHookAO webHookAO)
            {
                return webHookModelTransformer.transform(new DelegatingWebHookRegistrationParameters(webHookAO));
            }
        });
    }
}
