package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

import java.util.Map;

/**
 * Parameters with which the WebHook Listener is registered.
 */
@PublicSpi
public interface WebHookListenerRegistrationParameters
{
    /**
     * Returns the name of the WebHook Listener.
     */
    String getName();

    /**
     * Returns the url on which the Listeners listens for WebHooks.
     */
    String getUrl();

    /**
     * Returns the parameters of the WebHook Listener.
     */
    Map<String, Object> getParameters();

    /**
     * Returns all events for which the listener is registered.
     */
    Iterable<String> getEvents();

    /**
     * Returns if the listener is enabled.
     */
    Boolean isEnabled();
}
