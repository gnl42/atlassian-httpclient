package com.atlassian.webhooks.plugin;

import java.net.URI;

public interface ModuleDescriptorWebHookConsumerRegistry
{
    void register(String pluginKey, String webHookId, URI uri);

    void unregister(String pluginKey, String webHookId, URI uri);
}
