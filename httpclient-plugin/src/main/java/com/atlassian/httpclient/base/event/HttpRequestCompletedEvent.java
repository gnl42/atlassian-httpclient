package com.atlassian.httpclient.base.event;

import com.atlassian.analytics.api.annotations.Analytics;

import java.util.Map;

@Analytics("httpclient.requestcompleted")
public final class HttpRequestCompletedEvent extends AbstractHttpRequestEvent
{
    public HttpRequestCompletedEvent(String url, String httpMethod, int statusCode, long requestDuration, Map<String, String> properties)
    {
        super(url, httpMethod, statusCode, requestDuration, properties);
    }
}
