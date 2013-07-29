package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("webhooks")
public abstract class WebHookEvent
{
    private final String name;
    private final String url;
    private final Iterable<String> events;
    private final Map<String, Object> parameters;
    private final String registrationMethod;

    public WebHookEvent(String name, String url, Iterable<String> events, Map<String, Object> parameters, String registrationMethod)
    {
        this.name = name;
        this.url = url;
        this.events = events;
        this.parameters = parameters;
        this.registrationMethod = registrationMethod;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public Iterable<String> getEvents()
    {
        return events;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public String getRegistrationMethod()
    {
        return registrationMethod;
    }
}
