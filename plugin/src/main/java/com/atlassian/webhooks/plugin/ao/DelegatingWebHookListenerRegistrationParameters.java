package com.atlassian.webhooks.plugin.ao;

import com.atlassian.webhooks.plugin.service.WebHookListenerEventJoiner;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

/**
 */
public class DelegatingWebHookListenerRegistrationParameters implements WebHookListenerRegistrationParameters
{
    private final WebHookAO webHookAO;

    public DelegatingWebHookListenerRegistrationParameters(WebHookAO webHookAO)
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

}
