package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Cookie;
import com.atlassian.httpclient.api.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultCookieStore implements CookieStore {

    final org.apache.http.client.CookieStore delegate;

    public DefaultCookieStore()
    {
        delegate = new BasicCookieStore();
    }

    DefaultCookieStore(final org.apache.http.client.CookieStore cookieStore)
    {
        delegate = cookieStore;
    }

    public void addCookie(final Cookie cookie)
    {
        if (cookie instanceof DefaultCookie)
        {
            delegate.addCookie(((DefaultCookie) cookie).delegate);
        }
        else
        {
            throw new UnsupportedOperationException("Cookie type is not supported");
        }
    }

    public List<Cookie> getCookies()
    {
        List<Cookie> list = new ArrayList<Cookie>();
        for (org.apache.http.cookie.Cookie cookie : delegate.getCookies())
        {
            list.add(new DefaultCookie(cookie));
        }
        return list;
    }

    public boolean clearExpired(final Date date)
    {
        return delegate.clearExpired(date);
    }

    public void clear()
    {
        delegate.clear();
    }

}
