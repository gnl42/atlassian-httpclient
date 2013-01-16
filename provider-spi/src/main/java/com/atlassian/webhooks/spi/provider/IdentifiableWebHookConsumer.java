package com.atlassian.webhooks.spi.provider;

/**
 * Provides methods necessary to match a consumer with a webhook event.
 */
public interface IdentifiableWebHookConsumer
{
    String getPluginKey();

    String getConsumerKey();
}
