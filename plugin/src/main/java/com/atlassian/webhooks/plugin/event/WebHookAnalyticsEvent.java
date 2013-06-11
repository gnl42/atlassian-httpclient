package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks")
public class WebHookAnalyticsEvent
{
    private final String name;
    private final String url;
    private final String events;
    private final String filter;
    private final String registrationMethod;

    public WebHookAnalyticsEvent(String name, String url, String events, String parameters, String registrationMethod)
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

    public String getEvents()
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
