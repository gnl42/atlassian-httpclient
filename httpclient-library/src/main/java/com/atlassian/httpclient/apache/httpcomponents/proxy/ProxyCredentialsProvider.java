package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.google.common.collect.Iterables;
import io.atlassian.fugue.Option;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;

import static com.atlassian.httpclient.apache.httpcomponents.proxy.ProxyConfig.AuthenticationInfo;

/**
 * Configuration of credentials for proxy.
 */
public class ProxyCredentialsProvider implements CredentialsProvider {
    private final SystemDefaultCredentialsProvider delegate;

    private ProxyCredentialsProvider(final SystemDefaultCredentialsProvider delegate) {
        this.delegate = delegate;
    }

    public static Option<ProxyCredentialsProvider> build(final HttpClientOptions options) {
        final Iterable<AuthenticationInfo> authenticationInfos = Iterables.filter(
                ProxyConfigFactory.getProxyAuthentication(options),
                authenticationInfo -> authenticationInfo.getCredentials().isDefined());

        return Iterables.isEmpty(authenticationInfos) ? Option.<ProxyCredentialsProvider>none() : Option.some(createCredentialProvider(authenticationInfos));
    }

    private static ProxyCredentialsProvider createCredentialProvider(final Iterable<AuthenticationInfo> authenticationInfos) {
        final SystemDefaultCredentialsProvider credentialsProvider = new SystemDefaultCredentialsProvider();

        for (final AuthenticationInfo authenticationInfo : authenticationInfos) {
            authenticationInfo.getCredentials().foreach(credentials ->
                    credentialsProvider.setCredentials(authenticationInfo.getAuthScope(), credentials));
        }

        return new ProxyCredentialsProvider(credentialsProvider);
    }

    @Override
    public void setCredentials(final AuthScope authscope, final Credentials credentials) {
        delegate.setCredentials(authscope, credentials);
    }

    @Override
    public Credentials getCredentials(final AuthScope authscope) {
        return delegate.getCredentials(authscope);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
