package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface WebHookRegistry
{
    Iterable<String> getWebHookIds();

    Iterable<WebHookEvent> getWebHooks(Object event);
}
