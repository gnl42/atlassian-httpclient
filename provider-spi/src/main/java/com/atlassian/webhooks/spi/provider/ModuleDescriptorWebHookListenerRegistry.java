package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

import java.net.URI;

@PublicSpi
public interface ModuleDescriptorWebHookListenerRegistry
{
    void register(String webHookId, String pluginKey, URI uri, PluginModuleListenerParameters consumerParams);

    void unregister(String webHookId, String pluginKey, URI uri, PluginModuleListenerParameters consumerParams);
}
