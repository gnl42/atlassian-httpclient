package com.atlassian.webhooks.plugin.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.deleted")
public class WebHookAnalyticsDeletedEvent extends WebHookAnalyticsEvent
{
	public WebHookAnalyticsDeletedEvent(String name, String url, String events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
