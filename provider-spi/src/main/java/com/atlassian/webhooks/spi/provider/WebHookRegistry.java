package com.atlassian.webhooks.spi.provider;

public interface WebHookRegistry
{
    Iterable<String> getWebHookIds();

    Iterable<WebHookEvent> getWebHooks(Object event);
}
