package com.atlassian.httpclient.base.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.httpclient.api.Request;

@Analytics ("httpclient.requestfailed")
public final class HttpRequestFailedEvent extends AbstractHttpRequestEvent
{
    private final Throwable t;

    public HttpRequestFailedEvent(Request request, Throwable t)
    {
        super(request);
        this.t = t;
    }

    public Throwable cause()
    {
        return t;
    }
}
