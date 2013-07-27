package com.atlassian.webhooks.spi.provider.store;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Optional;

import java.util.Collection;

/**
 * Store to be implemented by products.
 */
@PublicSpi
public interface WebHookListenerStore
{
    /**
     * Adds a new WebHook listener and returns the newly created WebHook listener.
     *
     * @param name WebHook Listener name.
     * @param targetUrl url where response will be sent.
     * @param events list of events.
     * @param params parameters of the listener.
     * @param registrationMethod REST, UI or SERVICE.
     */
    public WebHookListenerParameters addWebHook(
            String name,
            String targetUrl,
            Iterable<String> events,
            String params,
            String registrationMethod);

    /**
     * Updates existing WebHook listener and returns the newly created WebHook.
     *
     * @param id WebHook listener id.
     * @param name WebHook listener name.
     * @param targetUrl url where response will be sent.
     * @param events list of events.
     * @param params parameters of the WebHook Listener.
     * @param enabled indicates whether a WebHook Listener is enabled.
     * @throws IllegalArgumentException when listener with the specified id doesn't exist.
     */
    public WebHookListenerParameters updateWebHook(
            int id,
            String name,
            String targetUrl,
            Iterable<String> events,
            String params,
            boolean enabled) throws IllegalArgumentException;

    /**
     * Get a single WebHook Listener by id.
     *
     * @param id of the WebHook Listener.
     * @return the WebHook listener.
     */
    Optional<WebHookListenerParameters> getWebHook(int id);

    /**
     * Removes single WebHook Listener by id.
     *
     * @param id of the WebHook Listener.
     * @throws IllegalArgumentException the specified id does not exist
     */
    void removeWebHook(int id) throws IllegalArgumentException;

    /**
     * Get a list of all listeners in the system
     * @return collection of WebHook listeners.
     */
    Collection<WebHookListenerParameters> getAllWebHooks();

    /**
     * Enables/disables WebHook listener.
     * @param id id of the listener to enable.
     * @param enabled true for enabling the listener, else false.
     * @return the changed listener, else none.
     * @throws IllegalArgumentException the specified id does not exist
     */
    public Optional<WebHookListenerParameters> enableWebHook(int id, boolean enabled);

}
