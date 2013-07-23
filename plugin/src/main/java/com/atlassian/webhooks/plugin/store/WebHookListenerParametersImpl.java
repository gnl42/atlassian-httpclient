package com.atlassian.webhooks.plugin.store;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;

import java.util.Date;

/**
 * Implementation of {@link WebHookListenerParameters}.
 */
public class WebHookListenerParametersImpl implements WebHookListenerParameters
{
    private final int id;
    private final boolean enabled;
    private final Date lastUpdated;
    private final String lastUpdatedUser;
    private final String name;
    private final String url;
    private final String parameters;
    private final Iterable<String> events;
    private final String registrationMethod;

    public static WebHookListenerParameters createWebHookListenerParameters(WebHookAO webHookAO)
    {
        return new WebHookListenerParametersImpl(webHookAO.getID(), webHookAO.isEnabled(), webHookAO.getLastUpdated(),
                webHookAO.getLastUpdatedUser(), webHookAO.getName(), webHookAO.getUrl(), webHookAO.getParameters(),
                WebHookListenerEventJoiner.split(webHookAO.getEvents()), webHookAO.getRegistrationMethod());
    }

    public WebHookListenerParametersImpl(int id, boolean enabled, Date lastUpdated, String lastUpdatedUser, String name,
            String url, String parameters, Iterable<String> events, String registrationMethod)
    {
        this.id = id;
        this.enabled = enabled;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedUser = lastUpdatedUser;
        this.name = name;
        this.url = url;
        this.parameters = parameters;
        this.events = events;
        this.registrationMethod = registrationMethod;
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    @Override
    public String getLastUpdatedUser()
    {
        return lastUpdatedUser;
    }

    @Override
    public String getRegistrationMethod()
    {
        return registrationMethod;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public String getParameters()
    {
        return parameters;
    }

    @Override
    public Iterable<String> getEvents()
    {
        return events;
    }
}
