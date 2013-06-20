package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.google.common.base.Optional;

/**
 * Internal service for registering, updating, removing and getting WebHook listeners.
 */
public interface InternalWebHookListenerService
{
    /**
     * Registers a new WebHook listener.
     *
     * @param name Unique name of the listener.
     * @param targetUrl Url to which data is posted.
     * @param events Events on which listener listens.
     * @param parameters Extra listeners parameters, such as jql filter.
     * @param registrationMethod Registration method, one of {@link com.atlassian.webhooks.plugin.manager.WebHookListenerManager.WebHookListenerRegistrationMethod}.
     * @return Newly created WebHook listener.
     */
    WebHookAO addWebHookListener(String name, String targetUrl, Iterable<String> events, String parameters, WebHookListenerManager.WebHookListenerRegistrationMethod registrationMethod);

    /**
     * Updates an existing WebHook listener.
     * @param id Id of the WebHook listener to update.
     * @param name Unique name of the listener.
     * @param targetUrl Url to which data is posted.
     * @param events Events on which listener listens.
     * @param parameters Extra listeners parameters, such as jql filter.
     * @param enabled True if enabled, false if the listener is disabled.
     * @return Updated WebHook listener.
     */
    WebHookAO updateWebHookListener(int id, String name, String targetUrl, Iterable<String> events, String parameters, boolean enabled);

    /**
     * Deletes the WebHook listener with given id.
     * @param id ID of the WebHook listener to remove.
     * @throws IllegalArgumentException If listener with specified id doesn't exist.
     */
    void removeWebHookListener(int id) throws IllegalArgumentException;

    /**
     * Finds the WebHook listener with given id, url, events and set of parameters.
     *
     * @param id Id of the listener to find.
     * @param url Url of the listener to find.
     * @param events Events of the listener to find.
     * @param parameters Parameters of the listener to find.
     * @return Found WebHook listener, or none.
     */
    Optional<WebHookAO> findWebHookListener(Integer id, String url, Iterable<String> events, String parameters);

    /**
     * Enables or disables the WebHook listener.
     * @param id Id of WebHook to enable/disable.
     * @param flag Flag, true if setting to enabled, else false.
     * @return Found and updated WebHook listener.
     */
    Optional<WebHookAO> enableWebHookListener(int id, boolean flag);

    /**
     * Returns the WebHook listener for given id.
     * @param id Id of the WebHook to return.
     * @return WebHook listener for given id, else None.
     */
    Optional<WebHookAO> getWebHookListener(int id);

    /**
     * Returns all listeners.
     * @return all WebHook listeners.
     */
    Iterable<WebHookAO> getAllWebHookListeners();

    /**
     * Clears the cache with WebHook listeners.
     */
    void clearWebHookListenerCache();
}
