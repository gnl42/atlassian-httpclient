package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.httpclient.api.factory.Scheme;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static com.atlassian.httpclient.api.factory.Scheme.HTTP;
import static com.atlassian.httpclient.api.factory.Scheme.HTTPS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public abstract class NoProxyHostsTestBase {

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] data() {
        return new Object[][] { { HTTP }, { HTTPS } };
    }

    @Parameterized.Parameter()
    public Scheme scheme;

    @Test
    public void blankNonProxyHosts() {
        setProxyDetails("localhost", 3128, "");

        ProxyConfig config = newProxyConfig();

        assertProxy(assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bar:9887"))),
                Proxy.Type.HTTP, "localhost", 3128);
    }

    @Test
    public void mixedNonProxyHosts() {
        setProxyDetails("localhost", 3128, "foo.*|bar|bing");

        ProxyConfig config = newProxyConfig();

        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bar:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.baz:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bing:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://bing:9887"))));
        assertProxy(assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://bar2:9887"))),
                Proxy.Type.HTTP, "localhost", 3128);
    }

    @Test
    public void mixedNonProxyHostsWithMixedCase() {
        setProxyDetails("localhost", 3128, "FOO.*|bar|BING");

        ProxyConfig config = newProxyConfig();

        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://FOO.BAR:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://fOo.bAz:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bing:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://bing:9887"))));
        assertProxy(assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://bar2:9887"))),
                Proxy.Type.HTTP, "localhost", 3128);
    }

    @Test
    public void multipleNonProxyHosts() {
        setProxyDetails("localhost", 3128, "foo.bar|foo.baz");

        ProxyConfig config = newProxyConfig();

        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bar:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.baz:9887"))));
        assertProxy(assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bing:9887"))),
                Proxy.Type.HTTP, "localhost", 3128);
    }

    @Test
    public void singleNonProxyHosts() {
        setProxyDetails("localhost", 3128, "foo.bar");

        ProxyConfig config = newProxyConfig();

        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bar:9887"))));
        assertProxy(assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.baz:9887"))),
                Proxy.Type.HTTP, "localhost", 3128);
    }

    @Test
    public void wildcardNonProxyHosts() {
        setProxyDetails("localhost", 3128, "foo.*|baz.*");

        ProxyConfig config = newProxyConfig();

        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bar:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.baz:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://foo.bing:9887"))));
        assertEquals(Proxy.NO_PROXY, assertSingleProxy(config.toProxySelector().select(toUri(scheme, "://baz.bar.com:9887"))));
    }

    protected abstract ProxyConfig newProxyConfig();

    protected abstract void setProxyDetails(String proxyHost, int proxyPort, String nonProxyHosts);

    private void assertProxy(Proxy proxy, Proxy.Type proxyType, String proxyHost, int proxyPort) {
        assertEquals(proxyType, proxy.type());
        assertTrue(proxy.address() instanceof InetSocketAddress);
        assertEquals(proxyHost, ((InetSocketAddress) proxy.address()).getHostName());
        assertEquals(proxyPort, ((InetSocketAddress) proxy.address()).getPort());
    }

    private Proxy assertSingleProxy(List<Proxy> proxies) {
        assertEquals(1, proxies.size());
        return proxies.get(0);
    }

    private URI toUri(Scheme scheme, String urlSuffix) {
        return URI.create(scheme.schemeName() + urlSuffix);
    }
}
