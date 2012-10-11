package com.atlassian.httpclient.base.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.httpclient.base.event.AbstractHttpRequestEvent;

import java.util.Map;

@Analytics("plugin.httprequestfailed")
public final class HttpRequestFailedEvent extends AbstractHttpRequestEvent
{
    public HttpRequestFailedEvent(String url, int statusCode, long elapsed, Map<String, String> properties)
    {
        super(url, statusCode, elapsed, properties);
    }

    public HttpRequestFailedEvent(String url, String error, long elapsed, Map<String, String> properties)
    {
        super(url, error, elapsed, properties);
    }
}
