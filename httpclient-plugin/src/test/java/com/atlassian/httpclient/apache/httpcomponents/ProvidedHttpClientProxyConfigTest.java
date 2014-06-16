package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.Scheme;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpHost;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ProvidedHttpClientProxyConfigTest
{
    @Test
    public void proxyConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 443, mock(LayeringStrategy.class));
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        proxies.put(Scheme.HTTP, new Host("localhost", 3128));
        HttpClientProxyConfig config = new ProvidedHttpClientProxyConfig(proxies, new HashMap<Scheme, List<String>>());
        Option<HttpHost> proxy = config.getProxy(scheme);
        assertThat(proxy.isDefined(), is(true));
        assertThat(proxy.get().getHostName(), is("localhost"));
        assertThat(proxy.get().getPort(), is(3128));
    }

    @Test
    public void noProxyConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 443, mock(LayeringStrategy.class));
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        proxies.put(Scheme.HTTPS, new Host("localhost", 3128));
        HttpClientProxyConfig config = new ProvidedHttpClientProxyConfig(proxies, new HashMap<Scheme, List<String>>());
        Option<HttpHost> proxy = config.getProxy(scheme);
        assertThat(proxy.isDefined(), is(false));
    }

    @Test
    public void nonProxyHostsConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 443, mock(LayeringStrategy.class));
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        proxies.put(Scheme.HTTP, new Host("localhost", 3128));
        Map<Scheme, List<String>> nonProxyHosts = new HashMap<Scheme, List<String>>();
        nonProxyHosts.put(Scheme.HTTP, ImmutableList.of("foo"));
        HttpClientProxyConfig config = new ProvidedHttpClientProxyConfig(proxies, nonProxyHosts);
        List<String> nonProxyHostList = config.getNonProxyHosts(scheme);
        assertThat(nonProxyHostList, contains("foo"));
    }

    @Test
    public void noNonProxyHostsConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 443, mock(LayeringStrategy.class));
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        proxies.put(Scheme.HTTPS, new Host("localhost", 3128));
        Map<Scheme, List<String>> nonProxyHosts = new HashMap<Scheme, List<String>>();
        nonProxyHosts.put(Scheme.HTTPS, ImmutableList.of("foo"));
        HttpClientProxyConfig config = new ProvidedHttpClientProxyConfig(proxies, nonProxyHosts);
        List<String> nonProxyHostList = config.getNonProxyHosts(scheme);
        assertThat(nonProxyHostList, Matchers.<String>empty());
    }
}
