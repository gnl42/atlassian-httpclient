package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface ModuleDescriptorWebHookListenerRegistry
{
    void register(String webHookId, String pluginKey, URI uri, PluginModuleListenerParameters consumerParams);

    void unregister(String webHookId, String pluginKey, URI uri, PluginModuleListenerParameters consumerParams);
}
