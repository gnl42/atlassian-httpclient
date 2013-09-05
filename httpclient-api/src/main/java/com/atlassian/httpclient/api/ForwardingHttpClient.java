package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.ForwardingObject;

/**
 * An HTTP Client which forwards all its method calls to another HTTP Client. Subclasses should override one or more
 * methods to modify the behavior of the backing HTTP Client as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 */
public abstract class ForwardingHttpClient extends ForwardingObject implements HttpClient
{
    /**
     * Constructor for use by subclasses.
     */
    protected ForwardingHttpClient()
    {}

    @Override
    protected abstract HttpClient delegate();

    @Override
    public Promise<Response> execute(Request request)
    {
        return delegate().execute(request);
    }
}
