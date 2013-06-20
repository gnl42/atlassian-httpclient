package com.atlassian.webhooks.plugin.manager;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface WebHookListenerManager
{
    public enum WebHookListenerRegistrationMethod
    {
        REST,
        UI,
        SERVICE
    }

    WebHookAO addWebHook(@NotNull String name, @NotNull String targetUrl, @NotNull String events, String params, WebHookListenerRegistrationMethod registrationMethod);

    /**
     * Updates existing WebHook listener and returns the newly created WebHook.
     *
     * @param id webhook id
     * @param name webhook name
     * @param targetUrl url where response will be sent
     * @param events list of events.
     * @param enabled indicates whether a webhook is enabled
     */
    WebHookAO updateWebHook(int id, String name, String targetUrl, String events, String params, boolean enabled) throws IllegalArgumentException;

    /**
     * Get a single webhook by id.
     *
     * @param id the webhook you want
     * @return the webhook
     */
    Optional<WebHookAO> getWebHook(int id);

    /**
     * Removes an existing webhook.
     *
     * @param id the webhook to be deleted
     * @throws IllegalArgumentException the specified id does not exist
     */
    void removeWebHook(int id) throws IllegalArgumentException;

    /**
     * Get a list of all webhooks in the system
     * @return collection of webhooks
     */
    Collection<WebHookAO> getAllWebHooks();

    Optional<WebHookAO> enableWebHook(int id, boolean enabled);

}
