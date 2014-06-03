package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.AbstractHttpAsyncClient;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Implementations of this interface provide proxy information for requests.
 *
 */
public interface HttpClientProxyConfig
{
    /**
     * Get the proxy to use for the given HTTP scheme.
     * @param scheme The scheme to provide the proxy for
     * @return Some HttpHost representing the proxy, or None if no proxy is configured for the scheme
     */
    Option<HttpHost> getProxy(@Nonnull final AsyncScheme scheme);

    /**
     * Get the list of hosts that don't need to use the proxy for the given HTTP scheme.
     * @param scheme The scheme.
     * @return List of non-proxy host strings that represent hosts that don't require a proxy.
     */
    List<String> getNonProxyHosts(@Nonnull final AsyncScheme scheme);

    /**
     * Configure the given client with credentials to access the configured proxy.
     * @param client The client to configure
     * @param schemeRegistry the registry of schemes
     */
    void applyProxyCredentials(@Nonnull final AbstractHttpAsyncClient client, @Nonnull final AsyncSchemeRegistry schemeRegistry);
}
