package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.httpclient.api.factory.Scheme;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ClearSystemProperties;

import static com.atlassian.httpclient.apache.httpcomponents.proxy.ProxyTestUtils.getProxyPropertiesNames;

public class NoProxyHostsSystemPropertiesTest extends NoProxyHostsTestBase {
    @Rule
    public ClearSystemProperties clearSystemPropertiesRule =
            new ClearSystemProperties(getProxyPropertiesNames());

    @Override
    protected SystemPropertiesProxyConfig newProxyConfig() {
        return new SystemPropertiesProxyConfig();
    }

    @Override
    protected void setProxyDetails(String proxyHost, int proxyPort, String nonProxyHosts) {
        System.setProperty(scheme.schemeName() + ".proxyHost", proxyHost);
        System.setProperty(scheme.schemeName() + ".proxyPort", Integer.toString(proxyPort));
        System.setProperty("http.nonProxyHosts", nonProxyHosts);
    }
}
