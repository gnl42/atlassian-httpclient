package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface ModuleDescriptorWebHookConsumerRegistry
{
    void register(String pluginKey, String webHookId, String consumerKey, URI uri);

    void unregister(String pluginKey, String webHookId, String consumerKey, URI uri);
}
