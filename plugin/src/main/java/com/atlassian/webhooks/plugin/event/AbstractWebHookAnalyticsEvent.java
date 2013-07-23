package com.atlassian.webhooks.plugin.event;

// todo unify with WebHookEvent
abstract class AbstractWebHookAnalyticsEvent
{
    private final String webHookId;
    private final String pluginKey;
    private final String uri;

    protected AbstractWebHookAnalyticsEvent(String webHookId, String pluginKey, String uri)
    {
        this.webHookId = webHookId;
        this.pluginKey = pluginKey;
        this.uri = uri;
    }

    public final String getWebHookId()
    {
        return webHookId;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public final String getUri()
    {
        return uri;
    }
}
