package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.Scheme;
import org.apache.http.HttpHost;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProvidedProxyConfigTest
{
    @Test
    public void proxyConfigured()
    {
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        proxies.put(Scheme.HTTP, new Host("localhost", 3128));
        ProxyConfig config = new ProvidedProxyConfig(proxies, new HashMap<Scheme, List<String>>());

        final Option<HttpHost> proxyHost = config.getProxyHost();

        assertThat(proxyHost.isDefined(), is(true));
        assertThat(proxyHost.get().getHostName(), is("localhost"));
        assertThat(proxyHost.get().getPort(), is(3128));
    }

    @Test
    public void noProxyConfigured()
    {
        Map<Scheme, Host> proxies = new HashMap<Scheme, Host>();
        ProxyConfig config = new ProvidedProxyConfig(proxies, new HashMap<Scheme, List<String>>());

        final Option<HttpHost> proxyHost = config.getProxyHost();
        assertThat(proxyHost.isDefined(), is(false));
    }
}
