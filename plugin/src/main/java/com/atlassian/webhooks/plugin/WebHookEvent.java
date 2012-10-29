package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventMatcher;

public interface WebHookEvent
{
    String getId();

    Object getEvent();

    EventMatcher<Object> getEventMatcher();

    String getJson();
}
