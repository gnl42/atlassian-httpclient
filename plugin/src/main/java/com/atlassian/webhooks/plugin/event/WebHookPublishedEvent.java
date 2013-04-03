package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.published")
public final class WebHookPublishedEvent extends AbstractWebHookEvent
{
    public WebHookPublishedEvent(String webHookId, String pluginKey, String uri)
    {
        super(webHookId, pluginKey, uri);
    }
}
