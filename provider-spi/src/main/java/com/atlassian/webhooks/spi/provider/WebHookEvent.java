package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface WebHookEvent
{
    /**
     * Id of the WebHookEvent in WebHook plugin. For instance, jira:issue_updated.
     */
    String getId();

    /**
     * Returns the actual event.
     */
    Object getEvent();

    /**
     * Returns the {@link EventMatcher} for the event.
     */
    EventMatcher<Object> getEventMatcher();

    /**
     * Serializes the event into JSON.
     */
    String getJson();
}
