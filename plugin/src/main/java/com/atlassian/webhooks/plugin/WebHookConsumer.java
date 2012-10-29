package com.atlassian.webhooks.plugin;

import java.net.URI;

public interface WebHookConsumer
{
    String getPluginKey();

    URI getPath();
}
