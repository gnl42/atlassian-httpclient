package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

/**
 * Fired when the web hook publishing queue is full and the event will be discarded
 */
@Analytics("webhooks.publishrejected")
public final class WebHookPublishRejectedAnalyticsEvent extends AbstractWebHookAnalyticsEvent
{
    private final String rejectionMessage;

    public WebHookPublishRejectedAnalyticsEvent(String webHookId, String pluginKey, String uri, String rejectionMessage)
    {
        super(webHookId, pluginKey, uri);
        this.rejectionMessage = rejectionMessage;
    }

    public String getRejectionMessage()
    {
        return rejectionMessage;
    }
}
