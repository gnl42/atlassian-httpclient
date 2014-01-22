package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProxyRoutePlanner implements HttpRoutePlanner
{
    private final AsyncSchemeRegistry schemeRegistry;

    public ProxyRoutePlanner(final AsyncSchemeRegistry schemeRegistry)
    {
        this.schemeRegistry = schemeRegistry;
    }

    @Override
    public HttpRoute determineRoute(final HttpHost targetHost, final HttpRequest request, final HttpContext httpContext)
            throws HttpException
    {
        checkNotNull(request);
        checkNotNull(targetHost);

        final HttpRoute forcedRoute = ConnRouteParams.getForcedRoute(request.getParams());
        if (forcedRoute != null)
        {
            return forcedRoute;
        }

        Option<HttpHost> proxy = getProxy(targetHost);
        return proxy.fold(new Supplier<HttpRoute>()
        {
            @Override
            public HttpRoute get()
            {
                return new HttpRoute(targetHost, ConnRouteParams.getLocalAddress(request.getParams()), isSecure(targetHost));
            }
        }, new Function<HttpHost, HttpRoute>()
        {
            @Override
            public HttpRoute apply(final HttpHost proxy)
            {
                return new HttpRoute(targetHost, ConnRouteParams.getLocalAddress(request.getParams()), proxy, isSecure(targetHost));
            }
        });
    }

    private Option<HttpHost> getProxy(final HttpHost targetHost)
    {
        AsyncScheme scheme = schemeRegistry.getScheme(targetHost);
        if (isNonProxyHost(targetHost))
        {
            return Option.none();
        }
        else
        {
            return HttpClientProxyConfig.getProxy(scheme);
        }
    }

    private boolean isNonProxyHost(final HttpHost targetHost)
    {
        return HttpClientProxyConfig.getNonProxyHosts(schemeRegistry.getScheme(targetHost)).contains(targetHost.getHostName());
    }

    private boolean isSecure(final HttpHost target)
    {
        AsyncScheme scheme = schemeRegistry.getScheme(target.getSchemeName());
        return "https".equalsIgnoreCase(scheme.getName());
    }
}
