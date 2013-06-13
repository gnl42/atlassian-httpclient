package com.atlassian.webhooks.plugin.ao;

import com.atlassian.webhooks.spi.provider.WebHookRegistrationParameters;

/**
 */
public class DelegatingWebHookRegistrationParameters implements WebHookRegistrationParameters
{
    private final WebHookAO webHookAO;

    public DelegatingWebHookRegistrationParameters(WebHookAO webHookAO)
    {
        this.webHookAO = webHookAO;
    }

    @Override
    public String getName()
    {
        return webHookAO.getName();
    }

    @Override
    public String getEvents()
    {
        return webHookAO.getEvents();
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

}
