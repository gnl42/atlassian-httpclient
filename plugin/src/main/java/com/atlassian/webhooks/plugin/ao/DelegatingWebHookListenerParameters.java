package com.atlassian.webhooks.plugin.ao;

import com.atlassian.webhooks.plugin.service.WebHookListenerEventJoiner;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;

import java.util.Date;

/**
 * This is the implementation of WebHookListenerParameters that retrieves all data about WebHook from AO.
 * This is done in order not to expose AO in SPI.
 */
public class DelegatingWebHookListenerParameters implements WebHookListenerParameters
{
    private final WebHookAO webHookAO;

    public DelegatingWebHookListenerParameters(WebHookAO webHookAO)
    {
        this.webHookAO = webHookAO;
    }

    @Override
    public String getName()
    {
        return webHookAO.getName();
    }

    @Override
    public Iterable<String> getEvents()
    {
        return WebHookListenerEventJoiner.split(webHookAO.getEvents());
    }

    @Override
    public String getUrl()
    {
        return webHookAO.getUrl();
    }

    @Override
    public String getParameters()
    {
        return webHookAO.getParameters();
    }

    @Override
    public Integer getId()
    {
        return webHookAO.getID();
    }

    @Override
    public boolean isEnabled()
    {
        return webHookAO.isEnabled();
    }

    @Override
    public Date getLastUpdated()
    {
        return webHookAO.getLastUpdated();
    }

    @Override
    public String getLastUpdatedUser()
    {
        return webHookAO.getLastUpdatedUser();
    }
}
