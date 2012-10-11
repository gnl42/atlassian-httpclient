package com.atlassian.webhooks.spi.provider;

/**
 * Provides multiple web hooks via registering with the registrar.
 */
public interface WebHookProvider
{
    void provide(WebHookRegistrar registrar);
}
