package com.atlassian.webhooks.plugin;


import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;

import java.net.URI;

public interface WebHookPublisher
{
    void register(String pluginKey, String eventIdentifier, URI path);

    void unregister(String pluginKey, String eventIdentifier, URI url);

    void publish(String eventIdentifier, EventMatcher<Object> eventMatcher, EventSerializer eventSerializer);
}
