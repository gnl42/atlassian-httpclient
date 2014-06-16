package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.factory.ProxyOptions;

/**
 * Creates a suitable HttpClientProxyConfig.
 */
public class ProxyConfigFactory
{
    /**
     * Create a suitable HttpClientProxyConfig from the given proxy options.
     * @param options The proxy options.
     * @return a suitable HttpClientProxyConfig configured with the proxy options.
     */
    public static Option<HttpClientProxyConfig> from(ProxyOptions options)
    {
        switch(options.getProxyMode())
        {
            case SYSTEM_PROPERTIES:
                return Option.<HttpClientProxyConfig>some(new SystemPropertiesHttpClientProxyConfig());
            case CONFIGURED:
                return Option.<HttpClientProxyConfig>some(new ProvidedHttpClientProxyConfig(options.getProxyHosts(), options.getNonProxyHosts()));
            default:
                return Option.none();
        }
    }

    private ProxyConfigFactory() {}
}
