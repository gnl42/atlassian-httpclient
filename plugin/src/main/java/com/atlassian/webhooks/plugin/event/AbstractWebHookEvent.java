package com.atlassian.webhooks.plugin.event;

import com.atlassian.webhooks.spi.provider.ConsumerKey;

abstract class AbstractWebHookEvent
{
    private final String webHookId;
    private final ConsumerKey consumerKey;
    private final String uri;

    protected AbstractWebHookEvent(String webHookId, ConsumerKey consumerKey, String uri)
    {
        this.webHookId = webHookId;
        this.consumerKey = consumerKey;
        this.uri = uri;
    }

    public final String getWebHookId()
    {
        return webHookId;
    }

    public ConsumerKey getConsumerKey()
    {
        return consumerKey;
    }

    public final String getUri()
    {
        return uri;
    }
}
