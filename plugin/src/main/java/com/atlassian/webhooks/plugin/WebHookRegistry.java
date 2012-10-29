package com.atlassian.webhooks.plugin;

public interface WebHookRegistry
{
    Iterable<String> getWebHookIds();

    Iterable<WebHookEvent> getWebHooks(Object event);
}
