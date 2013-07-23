package com.atlassian.webhooks.plugin.store;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.plugin.ao.WebHookListenerAO;
import com.google.common.base.Optional;
import net.java.ao.DBParam;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.validation.constraints.NotNull;

public class WebHookListenerStore
{
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
     * Adds a new WebHook listener and returns the newly created WebHook listener.
     *
     * @param name WebHook Listener name.
     * @param targetUrl url where response will be sent.
     * @param events list of events.
     * @param params parameters of the listener.
     * @param registrationMethod REST, UI or SERVICE.
     */
    public WebHookListenerAO addWebHook(
            @NotNull final String name,
            @NotNull final String targetUrl,
            @NotNull final Iterable<String> events,
            final String params,
            final WebHookListenerService.RegistrationMethod registrationMethod)
    {
        return ao.executeInTransaction(new TransactionCallback<WebHookListenerAO>()
        {
            @Override
            public WebHookListenerAO doInTransaction()
            {
                final WebHookListenerAO webHookListenerAO = ao.create(WebHookListenerAO.class,
                        new DBParam("LAST_UPDATED_USER", userManager.getRemoteUsername()),
                        new DBParam("URL", targetUrl),
                        new DBParam("LAST_UPDATED", new Date()),
                        new DBParam("NAME", name),
                        new DBParam("PARAMETERS", params == null ? "" : params),
                        new DBParam("REGISTRATION_METHOD", registrationMethod.name()),
                        new DBParam("EVENTS", WebHookListenerEventJoiner.join(events)),
                        new DBParam("ENABLED", true)
                );
                webHookListenerAO.save();
                return webHookListenerAO;
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
    public WebHookListenerAO updateWebHook(
            final int id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final String params,
            final boolean enabled) throws IllegalArgumentException
    {
        final Optional<WebHookListenerAO> originalWebHook = getWebHook(id);
        if (!originalWebHook.isPresent())
        {
            throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
        }
        return ao.executeInTransaction(new TransactionCallback<WebHookListenerAO>() {
            @Override
            public WebHookListenerAO doInTransaction() {
                final WebHookListenerAO webHook = ao.get(WebHookListenerAO.class, id);
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
    public Optional<WebHookListenerAO> getWebHook(final int id)
    {
        return ao.executeInTransaction(new TransactionCallback<Optional<WebHookListenerAO>>()
        {
            @Override
            public Optional<WebHookListenerAO> doInTransaction()
            {
                return Optional.fromNullable(ao.get(WebHookListenerAO.class, id));
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
        final Optional<WebHookListenerAO> webHookToDelete = getWebHook(id);
        if (!webHookToDelete.isPresent())
        {
            throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
        }
        ao.executeInTransaction(new TransactionCallback<Object>()
        {
            @Override
            public Object doInTransaction()
            {
                final WebHookListenerAO webHookListenerAO = ao.get(WebHookListenerAO.class, id);
                if (webHookListenerAO == null)
                {
                    throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
                }
                ao.delete(webHookListenerAO);
                return webHookListenerAO;
            }
        });
    }

    /**
     * Get a list of all listeners in the system
     * @return collection of WebHook listeners.
     */
    public Collection<WebHookListenerAO> getAllWebHooks()
    {
        return ao.executeInTransaction(new TransactionCallback<Collection<WebHookListenerAO>>()
        {
            @Override
            public Collection<WebHookListenerAO> doInTransaction()
            {
                return Arrays.asList(ao.find(WebHookListenerAO.class));
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
    public Optional<WebHookListenerAO> enableWebHook(final int id, final boolean enabled)
    {
        final WebHookListenerAO updatedWebHookListener = ao.executeInTransaction(new TransactionCallback<WebHookListenerAO>()
        {
            @Override
            public WebHookListenerAO doInTransaction()
            {
                final Optional<WebHookListenerAO> webHookAOOption = getWebHook(id);
                if (!webHookAOOption.isPresent())
                {
                    throw new IllegalArgumentException(i18n.getText("webhooks.invalid.webhook.id"));
                }
                final WebHookListenerAO webHookListenerAO = webHookAOOption.get();
                webHookListenerAO.setEnabled(enabled);
                webHookListenerAO.setLastUpdated(new Date());
                webHookListenerAO.setLastUpdatedUser(userManager.getRemoteUsername());
                webHookListenerAO.save();
                return webHookListenerAO;
            }
        });
        return Optional.fromNullable(updatedWebHookListener);
    }
}
