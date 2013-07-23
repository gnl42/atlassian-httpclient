package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface WebHookPublisher
{
    void publish(WebHookEvent webHookEvent);
}
