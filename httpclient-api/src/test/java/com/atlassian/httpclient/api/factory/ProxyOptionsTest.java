package com.atlassian.httpclient.api.factory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProxyOptionsTest
{
    @Test
    public void testProxyOptionBuilderDisablesConfigSettingsWithNoProxyMode()
    {
        Map<Scheme, Host> proxyHosts = ImmutableMap.of(Scheme.HTTP, new Host("foo", 8080));
        Map<Scheme, List<String>> nonProxyHosts = ImmutableMap.of(Scheme.HTTP, Lists.asList("Foo", new String[] {"Bar"}));
        ProxyOptions opts =
            ProxyOptions.ProxyOptionsBuilder.create().withProxy(proxyHosts, nonProxyHosts)
                .withNoProxy()
                .build();

        assertEquals(ProxyOptions.ProxyMode.NO_PROXY, opts.getProxyMode());
        assertEquals(0, opts.getProxyHosts().size());
        assertEquals(0, opts.getNonProxyHosts().size());
    }
    @Test
    public void testProxyOptionBuilderDisablesConfigSettingsWithSysPropsMode()
    {
        Map<Scheme, Host> proxyHosts = ImmutableMap.of(Scheme.HTTP, new Host("foo", 8080));
        Map<Scheme, List<String>> nonProxyHosts = ImmutableMap.of(Scheme.HTTP, Lists.asList("Foo", new String[] {"Bar"}));
        ProxyOptions opts =
                ProxyOptions.ProxyOptionsBuilder.create().withProxy(proxyHosts, nonProxyHosts)
                        .withDefaultSystemProperties()
                        .build();

        assertEquals(ProxyOptions.ProxyMode.SYSTEM_PROPERTIES, opts.getProxyMode());
        assertEquals(0, opts.getProxyHosts().size());
        assertEquals(0, opts.getNonProxyHosts().size());
    }

    @Test
    public void testProxyOptionBuilderWithProxyAndNonProxyHostConfig()
    {
        Map<Scheme, Host> proxyHosts = ImmutableMap.of(Scheme.HTTP, new Host("foo", 8080));
        Map<Scheme, List<String>> nonProxyHosts = ImmutableMap.of(Scheme.HTTP, Lists.asList("Foo", new String[] {"Bar"}));
        ProxyOptions opts =
                ProxyOptions.ProxyOptionsBuilder.create().withProxy(proxyHosts, nonProxyHosts)
                        .build();

        assertEquals(ProxyOptions.ProxyMode.CONFIGURED, opts.getProxyMode());
        assertEquals(proxyHosts, opts.getProxyHosts());
        assertEquals(nonProxyHosts, opts.getNonProxyHosts());
    }

    @Test
    public void testProxyOptionBuilderWithProxyConfig()
    {
        ProxyOptions opts =
                ProxyOptions.ProxyOptionsBuilder.create().withProxy(Scheme.HTTP, new Host("foo", 8080))
                        .build();

        assertEquals(ProxyOptions.ProxyMode.CONFIGURED, opts.getProxyMode());
        assertEquals(new Host("foo", 8080), opts.getProxyHosts().get(Scheme.HTTP));
        assertEquals(0, opts.getNonProxyHosts().size());
    }

}
