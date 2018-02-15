package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.Scheme;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;

public class NoProxyHostsProvidedConfigTest extends NoProxyHostsTestBase {
    private String proxyHost;
    private int proxyPort;
    private String nonProxyHosts;

    @Override
    protected ProxyConfig newProxyConfig() {
        return new ProvidedProxyConfig(
                ImmutableMap.of(scheme, new Host(proxyHost, proxyPort)),
                ImmutableMap.of(scheme, ImmutableList.copyOf(nonProxyHosts.split("\\|"))
                        .stream()
                        .map(StringUtils::trimToNull)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())));
    }

    @Override
    protected void setProxyDetails(String proxyHost, int proxyPort, String nonProxyHosts) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.nonProxyHosts = nonProxyHosts;
    }
}
