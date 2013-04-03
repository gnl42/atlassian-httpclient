package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface ModuleDescriptorWebHookConsumerRegistry
{
    void register(String webHookId, String pluginKey, URI uri, PluginModuleConsumerParams consumerParams);

    void unregister(String webHookId, String pluginKey, URI uri, PluginModuleConsumerParams consumerParams);
}
