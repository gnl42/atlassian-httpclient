package com.atlassian.webhooks.plugin.event;

import static com.google.common.base.Preconditions.*;

/**
 * Fired when the web hook publishing queue is full and the event will be discarded
 */
public final class WebHookPublishQueueFullEvent
{
    private final String webHookId;

    public WebHookPublishQueueFullEvent(String webHookId)
    {
        this.webHookId = checkNotNull(webHookId);
    }

    public String getWebHookId()
    {
        return webHookId;
    }
}
