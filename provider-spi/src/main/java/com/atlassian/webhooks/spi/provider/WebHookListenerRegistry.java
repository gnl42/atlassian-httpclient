package com.atlassian.webhooks.spi.provider;

public interface WebHookListenerRegistry
{
    Iterable<WebHookListener> getListeners(WebHookEvent webHookEvent);
}
