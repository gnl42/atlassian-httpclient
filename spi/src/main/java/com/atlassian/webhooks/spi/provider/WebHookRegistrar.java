package com.atlassian.webhooks.spi.provider;

/**
 * The fluent interface starting point for registering a web hook
 */
public interface WebHookRegistrar
{
    EventBuilder webhook(String id);
}
