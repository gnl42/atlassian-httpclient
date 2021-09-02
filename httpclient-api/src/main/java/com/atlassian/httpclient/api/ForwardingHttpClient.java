package com.atlassian.httpclient.api;


import com.google.common.collect.ForwardingObject;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * An HTTP Client which forwards all its method calls to another HTTP Client. Subclasses should override one or more
 * methods to modify the behavior of the backing HTTP Client as desired per the
 * <a href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see RedirectingHttpClient as new alternative for this class.
 * @deprecated This method will be removed on platform 6, since guava will be replaced by Java API.
 */
@Deprecated
public abstract class ForwardingHttpClient extends ForwardingObject implements HttpClient {
    /**
     * Constructor for use by subclasses.
     */
    protected ForwardingHttpClient() {
    }

    @Override
    protected abstract HttpClient delegate();

    @Override
    public Request.Builder newRequest() {
        return delegate().newRequest();
    }

    @Override
    public Request.Builder newRequest(final URI uri) {
        return delegate().newRequest(uri);
    }

    @Override
    public Request.Builder newRequest(final String uri) {
        return delegate().newRequest(uri);
    }

    @Override
    public Request.Builder newRequest(final URI uri, final String contentType, final String entity) {
        return delegate().newRequest(uri, contentType, entity);
    }

    @Override
    public Request.Builder newRequest(final String uri, final String contentType, final String entity) {
        return delegate().newRequest(uri, contentType, entity);
    }

    @Override
    public void flushCacheByUriPattern(final Pattern uriPattern) {
        delegate().flushCacheByUriPattern(uriPattern);
    }
}
