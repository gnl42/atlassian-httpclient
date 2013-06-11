package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.disabled")
public class WebHookAnalyticsDisabledEvent extends WebHookAnalyticsEvent
{
	public WebHookAnalyticsDisabledEvent(String name, String url, String events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
