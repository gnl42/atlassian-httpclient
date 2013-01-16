package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.webhooks.spi.provider.ConsumerKey;

@Analytics("webhooks.published")
public final class WebHookPublishedEvent extends AbstractWebHookEvent
{
    public WebHookPublishedEvent(String webHookId, ConsumerKey consumerKey, String uri)
    {
        super(webHookId, consumerKey, uri);
    }
}
