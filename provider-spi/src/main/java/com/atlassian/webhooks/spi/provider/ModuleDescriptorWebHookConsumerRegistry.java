package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface ModuleDescriptorWebHookConsumerRegistry
{
    void register(String webHookId, ConsumerKey consumerKey, URI uri);

    void unregister(String webHookId, ConsumerKey consumerKey, URI uri);
}
