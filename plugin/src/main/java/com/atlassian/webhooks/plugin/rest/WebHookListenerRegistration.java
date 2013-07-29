package com.atlassian.webhooks.plugin.rest;

import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(RegistrationParametersAdapter.class)
public class WebHookListenerRegistration implements WebHookListenerRegistrationParameters
{
	private final String name;
	private final String url;
	private final Map<String, Object> parameters;
    private final Iterable<String> events;
    private Boolean enabled;

    public WebHookListenerRegistration(String name, String url, Map<String, Object> parameters, Iterable<String> events, Boolean enabled)
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
    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    @Override
    public Iterable<String> getEvents()
    {
        return events;
    }

    @Override
    public Boolean isEnabled()
    {
        return enabled;
    }
}
