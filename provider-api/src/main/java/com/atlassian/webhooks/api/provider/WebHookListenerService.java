package com.atlassian.webhooks.api.provider;

import com.atlassian.annotations.PublicApi;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Optional;

// TODO we need some tests of this
// TODO ask Justin about adding ClearCacheEvent to some cross-product-plugin

/**
 * This WebHook listener service is responsible for adding, updating, removing and getting WebHookListeners registered
 * in atlassian-webhooks-plugin.  It may also be used to enforce cache clearing by the product-specific plugin.
 *
 * @since v1.0
 */
@PublicApi
public interface WebHookListenerService
{
    /**
     * Returns all WebHook listeners.
     *
     * @return a collection of WebHook listeners.
     */
    Iterable<WebHookListenerParameters> getAllWebHookListeners();

    /**
     * Returns a WebHook listener with given id.
     *
     * @param id The WebHook listener id.
     * @return The WebHook listener for given id, else None.
     */
    Optional<WebHookListenerParameters> getWebHookListener(Integer id);

    /**
     * Registers a new WebHook listener.
     *
     * @param webHookListenerParameters The parameters of WebHook listener to registerWebHookListener.
     * @return parameters of the registered WebHook listener.
     */
    WebHookListenerParameters registerWebHookListener(WebHookListenerParameters webHookListenerParameters);

    /**
     * Updates a WebHook listener with given id.
     *
     * @param id Id of the WebHook listener to updateWebHookListener.
     * @param webHookListenerParameters The parameters of WebHook listener to updateWebHookListener.
     * @return parameters of the updated WebHook listener.
     */
    WebHookListenerParameters updateWebHookListener(int id, WebHookListenerParameters webHookListenerParameters);

    /**
     * Deletes a WebHook listener with given id.
     *
     * @param id Id of WebHook listener to remove.
     */
    void deleteWebHookListener(int id);

    /**
     * Clears the cache with WebHook listeners.
     */
    void clearWebHookListenerCache();
}
