package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

/**
 * The fluent interface starting point for registering a web hook
 */
@PublicSpi
public interface WebHookRegistrar
{
    EventBuilder webhook(String id);
}
