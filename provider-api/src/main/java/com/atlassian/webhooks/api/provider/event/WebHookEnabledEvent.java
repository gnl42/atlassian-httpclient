package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.enabled")
public class WebHookEnabledEvent extends WebHookEvent
{
	public WebHookEnabledEvent(String name, String url, Iterable<String> events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
