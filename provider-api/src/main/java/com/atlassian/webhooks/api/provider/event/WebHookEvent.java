package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks")
public abstract class WebHookEvent
{
    private final String name;
    private final String url;
    private final Iterable<String> events;
    private final String filter;
    private final String registrationMethod;

    public WebHookEvent(String name, String url, Iterable<String> events, String parameters, String registrationMethod)
    {
        this.name = name;
        this.url = url;
        this.events = events;
        this.filter = parameters;
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

    public String getFilter()
    {
        return filter;
    }

    public String getRegistrationMethod()
    {
        return registrationMethod;
    }
}
