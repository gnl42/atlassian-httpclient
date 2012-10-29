package com.atlassian.webhooks.plugin;

public interface WebHookConsumerRegistry
{
    Iterable<WebHookConsumer> getConsumers(WebHookEvent webHookEvent);
}
