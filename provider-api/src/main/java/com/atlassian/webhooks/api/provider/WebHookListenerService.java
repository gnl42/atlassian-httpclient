package com.atlassian.webhooks.api.provider;

import com.atlassian.annotations.PublicApi;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import com.google.common.base.Optional;

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
     * @param registrationParameters The parameters of WebHook listener to register.
     * @param registrationMethod REST, SERVICE or UI.
     * @throws NullPointerException if any of required registration fields is null.
     * @throws IllegalArgumentException if required fields are illegal.
     * @throws NonUniqueRegistrationException if WebHook Listener with the same data already exists.
     * @return parameters of the registered WebHook listener or message collection.
     */
    WebHookListenerServiceResponse registerWebHookListener(WebHookListenerRegistrationParameters registrationParameters, RegistrationMethod registrationMethod);

    /**
     * Registers a new WebHook listener.
     *
     * @param registrationParameters The parameters of WebHook listener to register.
     * @throws NullPointerException if any of required registration fields is null.
     * @throws IllegalArgumentException if required fields are illegal.
     * @throws NonUniqueRegistrationException if WebHook Listener with the same data already exists.
     * @return parameters of the registered WebHook listener or a message collection.
     */
    WebHookListenerServiceResponse registerWebHookListener(WebHookListenerRegistrationParameters registrationParameters);

    /**
     * Updates a WebHook listener with given id.
     *
     * @param id Id of the WebHook listener to updateWebHookListener.
     * @param registrationParameters The parameters of WebHook listener to update.
     * @return parameters of the updated WebHook listener.
     * @throws NullPointerException if any of required registration fields is null.
     * @throws IllegalArgumentException if required fields are illegal or webhook with given id doesn't exist.
     * @throws NonUniqueRegistrationException if WebHook Listener with the same data already exists.
     */
    WebHookListenerServiceResponse updateWebHookListener(int id, WebHookListenerRegistrationParameters registrationParameters);

    /**
     * Deletes a WebHook listener with given id.
     * @throws IllegalArgumentException if WebHook with given id doesn't exist.
     * @param id Id of WebHook listener to remove.
     * @return a message collection with validation errors.
     */
    MessageCollection deleteWebHookListener(int id);

    /**
     * Enables or disables the WebHook listener.
     * @param id Id of WebHook to enable/disable.
     * @param flag Flag, true if setting to enabled, else false.
     * @throws IllegalArgumentException if WebHook with given id doesn't exist.
     * @return Found and updated WebHook listener.
     */
    Optional<WebHookListenerParameters> enableWebHookListener(int id, boolean flag);

    enum RegistrationMethod
    {
        REST,
        UI,
        SERVICE
    }

    class NonUniqueRegistrationException extends RuntimeException
    {
        private final Integer duplicateId;

        public NonUniqueRegistrationException(final String message, final Integer duplicateId)
        {
            super(message);
            this.duplicateId = duplicateId;
        }

        public Integer getDuplicateId()
        {
            return duplicateId;
        }
    }
}
