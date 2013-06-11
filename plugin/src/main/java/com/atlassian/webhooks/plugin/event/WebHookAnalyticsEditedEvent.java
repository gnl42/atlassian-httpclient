package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.edited")
public class WebHookAnalyticsEditedEvent extends WebHookAnalyticsEvent
{
	public WebHookAnalyticsEditedEvent(String name, String url, String events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
