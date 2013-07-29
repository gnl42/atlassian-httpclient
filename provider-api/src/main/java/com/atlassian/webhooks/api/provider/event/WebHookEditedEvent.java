package com.atlassian.webhooks.api.provider.event;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("webhooks.edited")
public class WebHookEditedEvent extends WebHookEvent
{
	public WebHookEditedEvent(String name, String url, Iterable<String> events, Map<String, Object> parameters, String registrationMethod)
    {
		super(name, url, events, parameters, registrationMethod);
	}
}
