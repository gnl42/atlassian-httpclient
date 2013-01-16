package com.atlassian.webhooks.spi.provider;

public interface WebHookConsumerRegistry
{
    Iterable<WebHookConsumer> getConsumers(WebHookEvent webHookEvent);
}
