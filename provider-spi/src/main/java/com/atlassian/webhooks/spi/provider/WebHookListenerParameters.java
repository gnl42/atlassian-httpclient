package com.atlassian.webhooks.spi.provider;

import java.util.Date;

public interface WebHookListenerParameters extends WebHookListenerRegistrationParameters
{
    /**
     * Returns id of the registered WebHook Listener.
     */
    Integer getId();

    /**
     * Returns true if the WebHook Listener is enabled, otherwise returns
     */
    boolean isEnabled();

    Date getLastUpdated();

    String getLastUpdatedUser();

}
