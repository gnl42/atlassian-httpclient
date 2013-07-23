package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

@Analytics("webhooks.edited")
public class WebHookEditedEvent extends WebHookEvent
{
	public WebHookEditedEvent(String name, String url, Iterable<String> events, String parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
