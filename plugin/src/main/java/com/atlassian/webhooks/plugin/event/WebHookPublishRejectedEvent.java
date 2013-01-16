package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.webhooks.spi.provider.ConsumerKey;

/**
 * Fired when the web hook publishing queue is full and the event will be discarded
 */
@Analytics("webhooks.publishrejected")
public final class WebHookPublishRejectedEvent extends AbstractWebHookEvent
{
    private final String rejectionMessage;

    public WebHookPublishRejectedEvent(String webHookId, ConsumerKey consumerKey, String uri, String rejectionMessage)
    {
        super(webHookId, consumerKey, uri);
        this.rejectionMessage = rejectionMessage;
    }

    public String getRejectionMessage()
    {
        return rejectionMessage;
    }
}
