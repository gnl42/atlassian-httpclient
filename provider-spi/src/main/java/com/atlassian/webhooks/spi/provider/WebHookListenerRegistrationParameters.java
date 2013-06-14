package com.atlassian.webhooks.spi.provider;

public interface WebHookListenerRegistrationParameters
{
    String getName();

    String getUrl();

    String getParameters();

    /**
     * Returns all events for which listener is registered.
     */
    Iterable<String> getEvents();
}
