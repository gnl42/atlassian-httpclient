package com.atlassian.webhooks.spi.provider;

public interface WebHookEvent
{
    String getId();

    Object getEvent();

    EventMatcher<Object> getEventMatcher();

    String getJson();
}
