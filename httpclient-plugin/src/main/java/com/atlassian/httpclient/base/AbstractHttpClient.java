package com.atlassian.httpclient.base;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;

import java.net.URI;

public abstract class AbstractHttpClient implements HttpClient
{
    @Override
    public Request.Builder newRequest()
    {
        return DefaultRequest.builder(this);
    }

    @Override
    public Request.Builder newRequest(URI uri)
    {
        return DefaultRequest.builder(this).setUri(uri);
    }

    @Override
    public Request.Builder newRequest(URI uri, String contentType, String entity)
    {
        return DefaultRequest.builder(this)
                .setContentType(contentType)
                .setEntity(entity)
                .setUri(uri);
    }

    @Override
    public Request.Builder newRequest(String uri)
    {
        return newRequest(URI.create(uri));
    }

    @Override
    public Request.Builder newRequest(String uri, String contentType, String entity)
    {
        return newRequest(URI.create(uri), contentType, entity);
    }
}
