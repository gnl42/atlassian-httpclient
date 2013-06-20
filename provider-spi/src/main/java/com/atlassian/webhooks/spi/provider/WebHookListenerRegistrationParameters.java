package com.atlassian.webhooks.spi.provider;

/**
 * Parameters with which the WebHook Listener is registered.
 */
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
    String getParameters();

    /**
     * Returns all events for which the listener is registered.
     */
    Iterable<String> getEvents();
}
