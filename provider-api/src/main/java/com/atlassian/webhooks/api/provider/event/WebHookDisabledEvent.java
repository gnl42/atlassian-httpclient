package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.disabled")
public class WebHookDisabledEvent extends WebHookEvent
{
	public WebHookDisabledEvent(String name, String url, Iterable<String> events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
