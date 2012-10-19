package com.atlassian.httpclient.api;


import com.google.common.collect.ForwardingObject;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * An HTTP Client which forwards all its method calls to another HTTP Client. Subclasses should override one or more
 * methods to modify the behavior of the backing HTTP Client as desired per the
 * <a href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 */
public abstract class ForwardingHttpClient extends ForwardingObject implements HttpClient
{
    /**
     * Constructor for use by subclasses.
     */
    protected ForwardingHttpClient()
    {
    }

    @Override
    protected abstract HttpClient delegate();

    @Override
    public Request newRequest()
    {
        return delegate().newRequest();
    }

    @Override
    public Request newRequest(URI uri)
    {
        return delegate().newRequest(uri);
    }

    @Override
    public Request newRequest(String uri)
    {
        return delegate().newRequest(uri);
    }

    @Override
    public Request newRequest(URI uri, String contentType, String entity)
    {
        return delegate().newRequest(uri, contentType, entity);
    }

    @Override
    public Request newRequest(String uri, String contentType, String entity)
    {
        return delegate().newRequest(uri, contentType, entity);
    }

    @Override
    public void flushCacheByUriPattern(Pattern uriPattern)
    {
        delegate().flushCacheByUriPattern(uriPattern);
    }
}
