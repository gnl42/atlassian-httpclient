package com.atlassian.webhooks.plugin.store;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;
import net.java.ao.DBParam;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.validation.constraints.NotNull;

public class WebHookListenerStore
{
    public enum WebHookListenerRegistrationMethod
    {
        REST,
        UI,
        SERVICE
    }

    private final ActiveObjects ao;
    private final UserManager userManager;
    private final I18nResolver i18n;

    public WebHookListenerStore(ActiveObjects ao, UserManager userManager, I18nResolver i18n)
    {
        this.ao = ao;
        this.userManager = userManager;
        this.i18n = i18n;
    }

    /**
     * Add a new WebHook listener and returns the newly created WebHook listener.
     *
     * @param name WebHook Listener name.
     * @param targetUrl url where response will be sent.
     * @param events list of events.
     * @param params parameters of the listener.
     * @param registrationMethod REST, UI or SERVICE.
     */
    public WebHookAO addWebHook(
            @NotNull final String name,
            @NotNull final String targetUrl,
            @NotNull final Iterable<String> events,
            final String params,
            final WebHookListenerRegistrationMethod registrationMethod)
    {
        return ao.executeInTransaction(new TransactionCallback<WebHookAO>()
        {
            @Override
            public WebHookAO doInTransaction()
            {
                final WebHookAO webHookAO = ao.create(WebHookAO.class,
                        new DBParam("LAST_UPDATED_USER", userManager.getRemoteUsername()),
                        new DBParam("URL", targetUrl),
                        new DBParam("LAST_UPDATED", new Date()),
                        new DBParam("NAME", name),
                        new DBParam("PARAMETERS", params == null ? "" : params),
                        new DBParam("REGISTRATION_METHOD", registrationMethod.name()),
                        new DBParam("EVENTS", WebHookListenerEventJoiner.join(events)),
                        new DBParam("ENABLED", true)
                );
                webHookAO.save();
                return webHookAO;
            }
        });
    }

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
    public WebHookAO updateWebHook(
            final int id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final String params,
            final boolean enabled) throws IllegalArgumentException
    {
        final Optional<WebHookAO> originalWebHook = getWebHook(id);
        if (!originalWebHook.isPresent())
        {
            throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
        }
        return ao.executeInTransaction(new TransactionCallback<WebHookAO>() {
            @Override
            public WebHookAO doInTransaction() {
                final WebHookAO webHook = ao.get(WebHookAO.class, id);
                webHook.setUrl(targetUrl);
                webHook.setName(name);
                webHook.setLastUpdatedUser(userManager.getRemoteUsername());
                webHook.setLastUpdated(new Date());
                webHook.setParameters(params);
                webHook.setEvents(WebHookListenerEventJoiner.join(events));
                webHook.setEnabled(enabled);

                webHook.save();

                return webHook;
            }
        });
    }

    /**
     * Get a single WebHook Listener by id.
     *
     * @param id of the WebHook Listener.
     * @return the WebHook listener.
     */
    public Optional<WebHookAO> getWebHook(final int id)
    {
        return ao.executeInTransaction(new TransactionCallback<Optional<WebHookAO>>()
        {
            @Override
            public Optional<WebHookAO> doInTransaction()
            {
                return Optional.fromNullable(ao.get(WebHookAO.class, id));
            }
        });
    }

    /**
     * Removes single WebHook Listener by id.
     *
     * @param id of the WebHook Listener.
     * @throws IllegalArgumentException the specified id does not exist
     */
    public void removeWebHook(final int id) throws IllegalArgumentException
    {
        final Optional<WebHookAO> webHookToDelete = getWebHook(id);
        if (!webHookToDelete.isPresent())
        {
            throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
        }
        ao.executeInTransaction(new TransactionCallback<Object>()
        {
            @Override
            public Object doInTransaction()
            {
                final WebHookAO webHookAO = ao.get(WebHookAO.class, id);
                if (webHookAO == null)
                {
                    throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
                }
                ao.delete(webHookAO);
                return webHookAO;
            }
        });
    }

    /**
     * Get a list of all listeners in the system
     * @return collection of WebHook listeners.
     */
    public Collection<WebHookAO> getAllWebHooks()
    {
        return ao.executeInTransaction(new TransactionCallback<Collection<WebHookAO>>()
        {
            @Override
            public Collection<WebHookAO> doInTransaction()
            {
                return Arrays.asList(ao.find(WebHookAO.class));
            }
        });
    }

    /**
     * Enables/disables WebHook listener.
     * @param id id of the listener to enable.
     * @param enabled true for enabling the listener, else false.
     * @return the changed listener, else none.
     * @throws IllegalArgumentException the specified id does not exist
     */
    public Optional<WebHookAO> enableWebHook(final int id, final boolean enabled)
    {
        final WebHookAO updatedWebHookListener = ao.executeInTransaction(new TransactionCallback<WebHookAO>()
        {
            @Override
            public WebHookAO doInTransaction()
            {
                final Optional<WebHookAO> webHookAOOption = getWebHook(id);
                if (!webHookAOOption.isPresent())
                {
                    throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
                }
                final WebHookAO webHookAO = webHookAOOption.get();
                webHookAO.setEnabled(enabled);
                webHookAO.setLastUpdated(new Date());
                webHookAO.setLastUpdatedUser(userManager.getRemoteUsername());
                webHookAO.save();
                return webHookAO;
            }
        });
        return Optional.fromNullable(updatedWebHookListener);
    }
}
