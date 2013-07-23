package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

/**
 * Provides multiple web hooks via registering with the registrar.
 */
@PublicSpi
public interface WebHookProvider
{
    void provide(WebHookRegistrar registrar);
}
