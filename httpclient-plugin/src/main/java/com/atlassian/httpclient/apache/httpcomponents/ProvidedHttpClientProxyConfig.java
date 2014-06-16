package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.Scheme;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.AbstractHttpAsyncClient;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpClientProxyConfig implementation that uses proxy configuration from construction, and not
 * from system properties.
 *
 * @since v6.3
 */
public class ProvidedHttpClientProxyConfig implements HttpClientProxyConfig
{
    private static final Logger log = LoggerFactory.getLogger(ProvidedHttpClientProxyConfig.class);

    private final Map<String, HttpHost> proxyHostMap;

    private final Map<String, List<String>> nonProxyHosts;

    public ProvidedHttpClientProxyConfig(@Nonnull final Map<Scheme, Host> proxyHostMap,
                                         @Nonnull final Map<Scheme, List<String>> nonProxyHosts)
    {
        Preconditions.checkNotNull(proxyHostMap);
        Preconditions.checkNotNull(nonProxyHosts);
        this.proxyHostMap = new HashMap<String, HttpHost>(proxyHostMap.size());
        for (Scheme s: proxyHostMap.keySet())
        {
            Host h = proxyHostMap.get(s);
            this.proxyHostMap.put(s.schemeName(), new HttpHost(h.getHost(), h.getPort()));
        }
        this.nonProxyHosts = new HashMap<String, List<String>>(nonProxyHosts.size());
        for (Scheme s: nonProxyHosts.keySet())
        {
            List<String> nonProxyHostList = nonProxyHosts.get(s);
            if (nonProxyHostList != null)
            {
                this.nonProxyHosts.put(s.schemeName(), ImmutableList.copyOf(nonProxyHostList));
            }
        }
    }

    @Override
    public Option<HttpHost> getProxy(@Nonnull AsyncScheme scheme)
    {
        return Option.option(proxyHostMap.get(scheme.getName().toLowerCase()));
    }

    @Override
    public List<String> getNonProxyHosts(@Nonnull AsyncScheme scheme)
    {
        List<String> list = nonProxyHosts.get(scheme.getName().toLowerCase());
        if (list != null)
        {
            return list;
        }
        else
        {
            return ImmutableList.of();
        }
    }

    @Override
    public void applyProxyCredentials(@Nonnull AbstractHttpAsyncClient client, @Nonnull AsyncSchemeRegistry schemeRegistry)
    {
        log.debug("Not configuring credentials for proxy (authentication not supported for programmatically configured proxies).");
    }

}
