package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

import java.util.Date;

@PublicSpi
public interface WebHookListenerParameters extends WebHookListenerRegistrationParameters
{
    /**
     * Returns id of the registered WebHook Listener.
     */
    Integer getId();

    /**
     * Returns true if the WebHook Listener is enabled, otherwise returns
     */
    Boolean isEnabled();

    Date getLastUpdated();

    String getLastUpdatedUser();

    String getRegistrationMethod();

}
