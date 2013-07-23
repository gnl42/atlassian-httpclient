package com.atlassian.webhooks.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

import java.util.Date;

public interface WebHookListenerAO extends Entity
{
    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getUrl();
    void setUrl(String url);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getName();
    void setName(String name);

    @NotNull
    String getLastUpdatedUser();
    void setLastUpdatedUser(String username);

    @NotNull
    Date getLastUpdated();
    void setLastUpdated(Date updated);

    @StringLength(StringLength.UNLIMITED)
    String getParameters();
    void setParameters(String parameters);

    // Was this created via REST, UI or SERVICE
    @NotNull
    String getRegistrationMethod();
    void setRegistrationMethod(String method);

    @StringLength(StringLength.UNLIMITED)
    String getEvents();
    void setEvents(String events);

    boolean isEnabled();
    void setEnabled(boolean enabled);
}
