package com.atlassian.webhooks.spi.provider;

public interface WebHookPublisher
{
    void publish(WebHookEvent webHookEvent);
}
