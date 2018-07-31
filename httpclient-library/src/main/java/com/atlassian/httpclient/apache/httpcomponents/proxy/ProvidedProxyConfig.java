package com.atlassian.httpclient.apache.httpcomponents.proxy;

import io.atlassian.fugue.Option;
import io.atlassian.fugue.Options;
import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.Scheme;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * HttpClientProxyConfig implementation that uses proxy configuration from construction, and not
 * from system properties.
 *
 * @since 0.20.0
 */
public class ProvidedProxyConfig extends ProxyConfig {
    private static final Logger log = LoggerFactory.getLogger(ProvidedProxyConfig.class);

    private static final List<Proxy> NO_PROXIES = Collections.singletonList(Proxy.NO_PROXY);
    private static final Iterable<String> SUPPORTED_SCHEMAS = Lists.newArrayList("http", "https");

    private final Map<String, HttpHost> proxyHostMap;
    private final Map<String, Predicate<String>> nonProxyHosts;

    public ProvidedProxyConfig(@Nonnull final Map<Scheme, Host> proxyHostMap,
                               @Nonnull final Map<Scheme, List<String>> nonProxyHosts) {
        Preconditions.checkNotNull(proxyHostMap);
        Preconditions.checkNotNull(nonProxyHosts);
        this.proxyHostMap = new HashMap<>(proxyHostMap.size());
        for (Scheme s : proxyHostMap.keySet()) {
            Host h = proxyHostMap.get(s);
            this.proxyHostMap.put(s.schemeName(), new HttpHost(h.getHost(), h.getPort()));
        }
        this.nonProxyHosts = new HashMap<>(nonProxyHosts.size());
        for (Scheme scheme : nonProxyHosts.keySet()) {
            List<String> nonProxyHostList = nonProxyHosts.get(scheme);
            if (nonProxyHostList != null) {
                Pattern wildcardHostsPattern = getWildcardHostsPattern(nonProxyHostList);
                Set<String> literalHosts = getLiteralHosts(nonProxyHostList);

                this.nonProxyHosts.put(scheme.schemeName(), host ->
                        literalHosts.contains(host) ||
                        wildcardHostsPattern != null && wildcardHostsPattern.matcher(host).matches());
            }
        }
    }

    private Set<String> getLiteralHosts(List<String> nonProxyHosts) {
        Set<String> literalHosts = nonProxyHosts.stream()
                .filter(host -> host.indexOf('*') == -1)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        log.trace("Literal hosts for http.nonProxyHost: {}", literalHosts);
        return literalHosts;
    }

    private Pattern getWildcardHostsPattern(List<String> nonProxyHosts) {
        String compoundPattern = nonProxyHosts.stream()
                .filter(host -> host.indexOf('*') != -1)
                .map(String::toLowerCase)
                .map(this::hostWildcardToPattern)
                .filter(Objects::nonNull)
                .map(subPattern -> "(:?" + subPattern + ")")
                .collect(Collectors.joining("|"));
        try {
            if (compoundPattern.isEmpty()) {
                return null;
            }

            log.trace("Compound pattern for http.nonProxyHost wildcard values {}: {}",
                    nonProxyHosts, compoundPattern);
            return Pattern.compile(compoundPattern);
        } catch (PatternSyntaxException e) {
            log.warn("Ignoring http.nonProxyHost values \"{}\" because converting these to a regular expression failed",
                    nonProxyHosts, e);
            return null;
        }
    }

    @Override
    Iterable<HttpHost> getProxyHosts() {
        final Iterable<Option<HttpHost>> httpHosts = Iterables.transform(SUPPORTED_SCHEMAS,
                schema -> Option.option(proxyHostMap.get(schema)));
        return Options.flatten(Options.filterNone(httpHosts));
    }

    @Override
    public Iterable<AuthenticationInfo> getAuthenticationInfo() {
        log.info("Authentication info not supported for ProvidedProxyConfig");
        return Collections.emptyList();
    }

    @Override
    public ProxySelector toProxySelector() {
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                String scheme = uri.getScheme().toLowerCase();
                String host = uri.getHost().toLowerCase();

                HttpHost proxyHost = proxyHostMap.get(scheme);
                if (proxyHost == null) {
                    return NO_PROXIES;
                }

                if (nonProxyMatch(scheme, host)) {
                    return NO_PROXIES;
                }

                // HTTP *OR* HTTPS despite what Proxy.Type.HTTP implies
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP,
                        InetSocketAddress.createUnresolved(proxyHost.getHostName(), proxyHost.getPort())));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // ignore
            }
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String compileHostPattern(String wildcardHost, String pattern) {
        String regex = "^" + pattern + "$";
        try {
            // compile the pattern but don't return it - we build a multi-pattern but don't want to abort the whole
            // thing if only one part fails for some reason
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            log.warn("Ignoring http.nonProxyHost \"{}\" because converting it to a regular expression failed", wildcardHost, e);
            return null;
        }
        return regex;
    }

    private String hostWildcardToPattern(String wildcardHost) {
        // to stay consistent with what ProxySelector.getDefault() returns for system properties, only the
        // leading/trailing * is noticed and only one or the other, not both
        if (wildcardHost.startsWith("*")) {
            return compileHostPattern(wildcardHost, ".*" + Pattern.quote(wildcardHost.substring(1)));
        } else if (wildcardHost.endsWith("*")) {
            return compileHostPattern(wildcardHost, Pattern.quote(wildcardHost.substring(0, wildcardHost.length() - 1)) + ".*");
        } else {
            return compileHostPattern(wildcardHost, Pattern.quote(wildcardHost));
        }
    }

    private Boolean nonProxyMatch(String scheme, String host) {
        return Optional.ofNullable(nonProxyHosts.get(scheme)).map(predicate -> predicate.test(host)).orElse(false);
    }
}
