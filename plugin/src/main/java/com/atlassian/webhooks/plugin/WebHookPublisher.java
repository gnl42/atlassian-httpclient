package com.atlassian.webhooks.plugin;

public interface WebHookPublisher
{
    void publish(WebHookEvent webHookEvent);
}
