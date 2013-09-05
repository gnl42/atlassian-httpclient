package com.atlassian.httpclient.base.event;

import com.atlassian.httpclient.api.Request;

abstract class AbstractHttpRequestEvent
{
    private final Request request;

    AbstractHttpRequestEvent(Request request)
    {
        this.request = request;
    }

    public Request request()
    {
        return request;
    }
}
