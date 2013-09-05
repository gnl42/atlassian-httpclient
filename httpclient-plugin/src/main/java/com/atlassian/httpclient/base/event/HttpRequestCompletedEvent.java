package com.atlassian.httpclient.base.event;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.httpclient.api.Request;

@Analytics ("httpclient.requestcompleted")
public final class HttpRequestCompletedEvent extends AbstractHttpRequestEvent
{
    private final long duration;

    public HttpRequestCompletedEvent(Request request, long duration)
    {
        super(request);
        this.duration = duration;
    }

    public long duration()
    {
        return duration;
    }
}
