package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.Date;

public class DefaultCookie implements Cookie {

    final org.apache.http.cookie.Cookie delegate;

    public DefaultCookie(final String name, final String value)
    {
        this(new BasicClientCookie(name, value));
    }

    DefaultCookie(final org.apache.http.cookie.Cookie cookie)
    {
        delegate = cookie;
    }

    public String getName()
    {
        return delegate.getName();
    }

    public String getValue()
    {
        return delegate.getValue();
    }

    public Date getExpiryDate()
    {
        return delegate.getExpiryDate();
    }

    public boolean isPersistent()
    {
        return delegate.isPersistent();
    }

    public String getDomain()
    {
        return delegate.getDomain();
    }

    public String getPath()
    {
        return delegate.getPath();
    }

    public boolean isSecure()
    {
        return delegate.isSecure();
    }

    public boolean isExpired(final Date date)
    {
        return delegate.isExpired(date);
    }
}
