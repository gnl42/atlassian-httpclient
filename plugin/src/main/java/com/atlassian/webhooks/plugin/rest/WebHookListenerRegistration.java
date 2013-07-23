package com.atlassian.webhooks.plugin.rest;

import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(RegistrationParametersAdapter.class)
public class WebHookListenerRegistration implements WebHookListenerRegistrationParameters
{
	private final String name;
	private final String url;
	private final String parameters;
    private final Iterable<String> events;
    private boolean enabled;

    public WebHookListenerRegistration(String name, String url, String parameters, Iterable<String> events, final boolean enabled)
    {
        this.name = name;
        this.url = url;
        this.parameters = parameters;
        this.events = events;
        this.enabled = enabled;
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

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
}
