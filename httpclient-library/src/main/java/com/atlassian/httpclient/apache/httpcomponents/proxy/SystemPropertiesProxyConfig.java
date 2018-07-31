package com.atlassian.httpclient.apache.httpcomponents.proxy;

import io.atlassian.fugue.Option;
import io.atlassian.fugue.Options;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import java.net.ProxySelector;

/**
 * Proxy configuration set with system properties.
 */
@SuppressWarnings("StaticPseudoFunctionalStyleMethod")
public class SystemPropertiesProxyConfig extends ProxyConfig {
    private static final Iterable<String> SUPPORTED_SCHEMAS = Lists.newArrayList("http", "https");

    Iterable<HttpHost> getProxyHosts() {
        return Options.flatten(Options.filterNone(
                Iterables.transform(SUPPORTED_SCHEMAS, SystemPropertiesProxyConfig::getProxy)));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Iterable<AuthenticationInfo> getAuthenticationInfo() {
        return Iterables.transform(getProxyHosts(), httpHost -> {
            final AuthScope authScope = new AuthScope(httpHost);
            final Option<Credentials> credentials = credentialsForScheme(httpHost.getSchemeName());
            return new AuthenticationInfo(authScope, credentials);
        });
    }

    @Override
    public ProxySelector toProxySelector() {
        // the default ProxySelector will just do the right thing
        return ProxySelector.getDefault();
    }

    private static Option<HttpHost> getProxy(final String schemeName) {
        String proxyHost = System.getProperty(schemeName + ".proxyHost");
        if (proxyHost != null) {
            return Option.some(new HttpHost(proxyHost, Integer.parseInt(System.getProperty(schemeName + ".proxyPort")), schemeName));
        } else {
            return Option.none();
        }
    }

    private static Option<Credentials> credentialsForScheme(final String schemeName) {
        final String username = System.getProperty(schemeName + ".proxyUser");
        if (username != null) {
            final String proxyPassword = System.getProperty(schemeName + ".proxyPassword");
            final String proxyAuth = System.getProperty(schemeName + ".proxyAuth");
            if (proxyAuth == null || proxyAuth.equalsIgnoreCase("basic")) {
                return Option.<Credentials>some(new UsernamePasswordCredentials(username, proxyPassword));
            } else if (proxyAuth.equalsIgnoreCase("digest") || proxyAuth.equalsIgnoreCase("ntlm")) {
                String ntlmDomain = System.getProperty(schemeName + ".proxyNtlmDomain");
                String ntlmWorkstation = System.getProperty(schemeName + ".proxyNtlmWorkstation");
                return Option.<Credentials>some(new NTCredentials(username, proxyPassword,
                        StringUtils.defaultString(ntlmWorkstation), StringUtils.defaultString(ntlmDomain)));
            } else {
                return Option.none();
            }
        } else {
            return Option.none();
        }
    }

}
