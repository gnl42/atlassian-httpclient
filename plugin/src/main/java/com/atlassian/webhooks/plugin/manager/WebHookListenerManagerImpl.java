package com.atlassian.webhooks.plugin.manager;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;
import net.java.ao.DBParam;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class WebHookListenerManagerImpl implements WebHookListenerManager
{
    private final ActiveObjects ao;
    private final UserManager userManager;
    private final I18nResolver i18n;

    public WebHookListenerManagerImpl(ActiveObjects ao, UserManager userManager, I18nResolver i18n)
    {
        this.ao = ao;
        this.userManager = userManager;
        this.i18n = i18n;
    }

    @Override
    public WebHookAO addWebHook(@NotNull final String name, @NotNull final String targetUrl, @NotNull final String events, final String params, final WebHookListenerRegistrationMethod registrationMethod)
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
                        new DBParam("EVENTS", events),
                        new DBParam("ENABLED", true)
                );
                webHookAO.save();
                return webHookAO;
            }
        });
    }

    @Override
    public WebHookAO updateWebHook(final int id, final String name, final String targetUrl, final String events, final String params, final boolean enabled) throws IllegalArgumentException
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
                webHook.setEvents(events);
                webHook.setEnabled(enabled);

                webHook.save();

                return webHook;
            }
        });
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Optional<WebHookAO> enableWebHook(final int id, final boolean enabled)
    {
        final WebHookEnablementResult enablementResult = ao.executeInTransaction(new TransactionCallback<WebHookEnablementResult>()
        {
            @Override
            public WebHookEnablementResult doInTransaction()
            {
                boolean wasEnabled = false;
                final Optional<WebHookAO> webHookAO = getWebHook(id);
                if (webHookAO.isPresent())
                {
                    final WebHookAO webHook = webHookAO.get();
                    wasEnabled = webHook.isEnabled();
                    webHook.setEnabled(enabled);
                    webHook.setLastUpdated(new Date());
                    webHook.setLastUpdatedUser(userManager.getRemoteUsername());
                    webHook.save();
                }
                return new WebHookEnablementResult(webHookAO, wasEnabled);
            }
        });

        return enablementResult.updatedWebhook;
    }

    static class WebHookEnablementResult
    {
        final Optional<WebHookAO> updatedWebhook;
        final boolean wasEnabled;

        WebHookEnablementResult(Optional<WebHookAO> updatedWebhook, boolean wasEnabled)
        {
            this.updatedWebhook = updatedWebhook;
            this.wasEnabled = wasEnabled;
        }
    }
}
