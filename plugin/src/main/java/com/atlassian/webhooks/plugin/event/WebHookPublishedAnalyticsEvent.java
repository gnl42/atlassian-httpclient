package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.published")
public final class WebHookPublishedAnalyticsEvent extends AbstractWebHookAnalyticsEvent
{
    public WebHookPublishedAnalyticsEvent(String webHookId, String pluginKey, String uri)
    {
        super(webHookId, pluginKey, uri);
    }
}
