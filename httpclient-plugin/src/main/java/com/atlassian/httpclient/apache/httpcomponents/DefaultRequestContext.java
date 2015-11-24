package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.CookieStore;
import com.atlassian.httpclient.api.RequestContext;
import org.apache.http.client.protocol.HttpClientContext;

public class DefaultRequestContext implements RequestContext {
    final HttpClientContext delegate;

    public DefaultRequestContext()
    {
        delegate = new HttpClientContext();
    }
    
    public Object getAttribute(String id)
    {
        return delegate.getAttribute(id);
    }

    public DefaultCookieStore getCookieStore()
    {
        org.apache.http.client.CookieStore cookieStore = delegate.getCookieStore();
        return (cookieStore == null) ? null : new DefaultCookieStore(cookieStore);
    }

    public Object removeAttribute(final String id)
    {
        return delegate.removeAttribute(id);
    }

    public void setAttribute(final String id, final Object obj)
    {
        delegate.setAttribute(id, obj);
    }

    public void setCookieStore(final CookieStore cookieStore)
    {
        delegate.setCookieStore(cookieStore instanceof DefaultCookieStore ?
                ((DefaultCookieStore) cookieStore).delegate : null);
    }

}
