package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.google.common.collect.Lists;
import org.apache.http.HttpHost;

import static com.atlassian.httpclient.apache.httpcomponents.proxy.ProxyConfig.AuthenticationInfo;

public class ProxyConfigFactory {
    public static Option<HttpHost> getProxyHost(final HttpClientOptions options) {
        return getProxyConfig(options).fold(Option::none, ProxyConfig::getProxyHost);
    }

    public static Iterable<AuthenticationInfo> getProxyAuthentication(final HttpClientOptions options) {
        return getProxyConfig(options).fold(Lists::newLinkedList, ProxyConfig::getAuthenticationInfo);
    }

    public static Option<ProxyConfig> getProxyConfig(final HttpClientOptions options) {
        final Option<ProxyConfig> config;
        switch (options.getProxyOptions().getProxyMode()) {
            case SYSTEM_PROPERTIES:
                config = Option.some(new SystemPropertiesProxyConfig());
                break;
            case CONFIGURED:
                config = Option.some(new ProvidedProxyConfig(
                        options.getProxyOptions().getProxyHosts(), options.getProxyOptions().getNonProxyHosts()));
                break;
            default:
                config = Option.none();
        }
        return config;
    }
}
