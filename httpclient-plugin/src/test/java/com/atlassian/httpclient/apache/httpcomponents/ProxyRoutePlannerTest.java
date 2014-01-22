package com.atlassian.httpclient.apache.httpcomponents;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ProxyRoutePlannerTest
{
    private final ProxyRoutePlanner proxyRoutePlanner = new ProxyRoutePlanner(AsyncSchemeRegistryFactory.createDefault());

    @Test
    public void proxyConfiguredForHttpRequest() throws HttpException
    {
        System.setProperty("https.nonProxyHosts", "localhost|one-more-host.com");
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "3128");

        HttpPost post = new HttpPost("http://localhost2/x");
        HttpRoute route = proxyRoutePlanner.determineRoute(new HttpHost("localhost2", 80, "http"), post, mock(HttpContext.class));

        assertThat(route.getProxyHost(), notNullValue(HttpHost.class));
        assertThat(route.getProxyHost().getHostName(), is("localhost"));
        assertThat(route.getProxyHost().getPort(), is(3128));
        assertThat(route.getProxyHost().getSchemeName(), is("http"));
        assertThat(route.isSecure(), is(false));
        assertThat(route.getTargetHost().getHostName(), is("localhost2"));
    }

    @Test
    public void proxyConfiguredForHttpsRequest() throws HttpException
    {
        System.setProperty("https.nonProxyHosts", "localhost|one-more-host.com");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");

        HttpPost post = new HttpPost("https://localhost2/x");
        HttpRoute route = proxyRoutePlanner.determineRoute(new HttpHost("localhost2", 443, "https"), post, mock(HttpContext.class));

        assertThat(route.getProxyHost(), notNullValue(HttpHost.class));
        assertThat(route.getProxyHost().getHostName(), is("localhost"));
        assertThat(route.getProxyHost().getPort(), is(3128));
        assertThat(route.getProxyHost().getSchemeName(), is("https"));
        assertThat(route.isSecure(), is(true));
        assertThat(route.getTargetHost().getHostName(), is("localhost2"));
    }

    @Test
    public void noProxyConfigured() throws HttpException
    {
        HttpPost post = new HttpPost("https://localhost2/x");
        HttpRoute route = proxyRoutePlanner.determineRoute(new HttpHost("localhost2", 443, "https"), post, mock(HttpContext.class));

        assertThat(route.getProxyHost(), nullValue(HttpHost.class));
    }

    @Test
    public void nonProxyHostTarget() throws HttpException
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("https.nonProxyHosts", "localhost|one-more-host.com");

        HttpPost post = new HttpPost("https://localhost/x");
        HttpRoute route = proxyRoutePlanner.determineRoute(new HttpHost("localhost", 443, "https"), post, mock(HttpContext.class));

        assertThat(route.getProxyHost(), nullValue(HttpHost.class));
    }

    @After
    public void tearDown() throws Exception
    {
        System.setProperties(new Properties());
    }
}
