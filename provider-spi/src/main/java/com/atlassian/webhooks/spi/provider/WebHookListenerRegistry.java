package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface WebHookListenerRegistry
{
    Iterable<WebHookListener> getListeners(WebHookEvent webHookEvent);
}
