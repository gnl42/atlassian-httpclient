package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.created")
public class WebHookAnalyticsCreatedEvent extends WebHookAnalyticsEvent
{
	public WebHookAnalyticsCreatedEvent(String name, String url, String events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
