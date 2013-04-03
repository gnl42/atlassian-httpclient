package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface WebHookConsumer
{
    String getPluginKey();

    URI getPath();

    Object getConsumerParams();
}
