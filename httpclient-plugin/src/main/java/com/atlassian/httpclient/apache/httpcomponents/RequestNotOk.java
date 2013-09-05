package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpStatus;

public class RequestNotOk extends Throwable
{
    private static final long serialVersionUID = -58242015599491937L;

    private final HttpStatus status;

    public RequestNotOk(HttpStatus status)
    {
        super();
        this.status = status;
    }

    public HttpStatus status()
    {
        return status;
    }
}
