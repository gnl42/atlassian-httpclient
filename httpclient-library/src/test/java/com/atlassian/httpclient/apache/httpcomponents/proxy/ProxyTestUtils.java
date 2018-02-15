package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.httpclient.api.factory.Scheme;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.atlassian.httpclient.api.factory.Scheme.HTTP;
import static com.atlassian.httpclient.api.factory.Scheme.HTTPS;

public class ProxyTestUtils {
    public static String[] getProxyPropertiesNames() {
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Scheme scheme : Lists.newArrayList(HTTP, HTTPS)) {
            for (String property : Lists.newArrayList("nonProxyHosts", "proxyHost", "proxyPort", "proxyUser", "proxyPassword")) {
                builder.add(scheme.schemeName() + "." + property);
            }
        }
        return builder.build().toArray(new String[8]);
    }
}
