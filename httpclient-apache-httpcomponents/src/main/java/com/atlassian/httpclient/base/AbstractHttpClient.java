package com.atlassian.httpclient.base;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;

import java.net.URI;

public abstract class AbstractHttpClient implements HttpClient
{
    @Override
    public Request newRequest()
    {
        return new DefaultRequest(this);
    }

    @Override
    public Request newRequest(URI uri)
    {
        return new DefaultRequest(this, uri);
    }

    @Override
    public Request newRequest(URI uri, String contentType, String entity)
    {
        return new DefaultRequest(this, uri, contentType, entity);
    }

    @Override
    public Request newRequest(String uri)
    {
        return newRequest(URI.create(uri));
    }

    @Override
    public Request newRequest(String uri, String contentType, String entity)
    {
        return newRequest(URI.create(uri), contentType, entity);
    }

    public abstract ResponsePromise execute(DefaultRequest request);
}
