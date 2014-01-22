package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.nio.client.AbstractHttpAsyncClient;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;

import java.util.List;

public class HttpClientProxyConfig
{
    private static Splitter NON_PROXY_HOST_SPLITTER = Splitter.on('|');

    private HttpClientProxyConfig() {}

    public static Option<HttpHost> getProxy(final AsyncScheme scheme)
    {
        return getProxy(scheme.getName().toLowerCase());
    }

    private static Option<HttpHost> getProxy(final String schemeName)
    {
        String proxyHost = System.getProperty(schemeName + ".proxyHost");
        if (proxyHost != null)
        {
            return Option.some(new HttpHost(proxyHost, Integer.parseInt(System.getProperty(schemeName + ".proxyPort")), schemeName));
        }
        else
        {
            return Option.none();
        }
    }

    public static List<String> getNonProxyHosts(final AsyncScheme scheme)
    {
        return getNonProxyHosts(scheme.getName().toLowerCase());
    }

    private static List<String> getNonProxyHosts(final String schemeName)
    {
        String nonProxyHosts = System.getProperty(schemeName + ".nonProxyHosts");
        if (nonProxyHosts != null)
        {
            return Lists.newArrayList(NON_PROXY_HOST_SPLITTER.split(nonProxyHosts));
        }
        else
        {
            return ImmutableList.of();
        }
    }

    public static void applyProxyCredentials(final AbstractHttpAsyncClient client, final AsyncSchemeRegistry schemeRegistry)
    {
        for (String scheme : schemeRegistry.getSchemeNames())
        {
            applyProxyCredentials(client, scheme.toLowerCase());
        }
    }

    private static void applyProxyCredentials(final AbstractHttpAsyncClient client, final String schemeName)
    {
        final Option<HttpHost> proxy = getProxy(schemeName);
        proxy.foreach(new Effect<HttpHost>()
        {
            @Override
            public void apply(final HttpHost httpHost)
            {
                ProxyAuthentication.forScheme(schemeName).foreach(new Effect<ProxyAuthentication>()
                {
                    @Override
                    public void apply(final ProxyAuthentication proxyAuthentication)
                    {
                        proxyAuthentication.apply(client, httpHost);
                    }
                });
            }
        });
    }

    private static class ProxyAuthentication
    {
        private final String scheme;
        private final Credentials credentials;

        ProxyAuthentication(final String scheme, final Credentials credentials)
        {
            this.scheme = scheme;
            this.credentials = credentials;
        }

        public void apply(final AbstractHttpAsyncClient client, final HttpHost httpHost)
        {
            AuthScope scope = new AuthScope(httpHost.getHostName(), httpHost.getPort(), null, scheme);
            client.getCredentialsProvider().setCredentials(scope, credentials);
        }

        public static Option<ProxyAuthentication> forScheme(final String schemeName)
        {
            final String username = System.getProperty(schemeName + ".proxyUser");
            if (username != null)
            {
                final String proxyPassword = System.getProperty(schemeName + ".proxyPassword");
                final String proxyAuth = System.getProperty(schemeName + ".proxyAuth");
                if (proxyAuth == null || proxyAuth.equalsIgnoreCase("basic"))
                {
                    return Option.some(new ProxyAuthentication(proxyAuth, new UsernamePasswordCredentials(username, proxyPassword)));
                }
                else if (proxyAuth.equalsIgnoreCase("digest") || proxyAuth.equalsIgnoreCase("ntlm"))
                {
                    String ntlmDomain = System.getProperty(schemeName + ".proxyNtlmDomain");
                    String ntlmWorkstation = System.getProperty(schemeName + ".proxyNtlmWorkstation");
                    return Option.some(new ProxyAuthentication(proxyAuth, new NTCredentials(username, proxyPassword,
                            StringUtils.defaultString(ntlmWorkstation), StringUtils.defaultString(ntlmDomain))));
                }
                else
                {
                    return Option.none();
                }
            }
            else
            {
                return Option.none();
            }
        }
    }

}
