package com.atlassian.webhooks.plugin.store;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.api.provider.WebHookUpgradeTaskStore;
import com.atlassian.webhooks.plugin.ao.WebHookListenerAO;
import net.java.ao.DBParam;

import java.util.Date;

public class WebHookListenerUpgradeTaskStore implements WebHookUpgradeTaskStore
{
    private final ActiveObjects ao;

    public WebHookListenerUpgradeTaskStore(ActiveObjects ao)
    {
        this.ao = ao;
    }

    @Override
    public boolean saveWebHook(
            final Integer id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final String params,
            final boolean enabled,
            final String username,
            final Date lastUpdatedDate,
            final WebHookListenerService.RegistrationMethod registrationMethod)
    {
        return ao.executeInTransaction(new TransactionCallback<Boolean>()
        {
            @Override
            public Boolean doInTransaction()
            {
            final WebHookListenerAO webHookListenerAO = ao.create(WebHookListenerAO.class,
                    new DBParam("ID", id),
                    new DBParam("LAST_UPDATED_USER", username),
                    new DBParam("URL", targetUrl),
                    new DBParam("LAST_UPDATED", lastUpdatedDate),
                    new DBParam("NAME", name),
                    new DBParam("PARAMETERS", params == null ? "" : params),
                    new DBParam("REGISTRATION_METHOD", registrationMethod.name()),
                    new DBParam("EVENTS", WebHookListenerEventJoiner.join(events)),
                    new DBParam("ENABLED", enabled)
            );
            webHookListenerAO.save();
            return true;
            }
        });
    }
}
