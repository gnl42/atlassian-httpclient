package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.enabled")
public class WebHookAnalyticsEnabledEvent extends WebHookAnalyticsEvent
{
	public WebHookAnalyticsEnabledEvent(String name, String url, String events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
