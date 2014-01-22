package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.nio.client.AbstractHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Properties;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpClientProxyConfigTest
{
    @Test
    public void httpsProxyConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("https", 443, mock(LayeringStrategy.class));

        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");
        Option<HttpHost> proxy = HttpClientProxyConfig.getProxy(scheme);

        assertThat(proxy.isDefined(), is(true));
        assertThat(proxy.get().getHostName(), is("localhost"));
        assertThat(proxy.get().getPort(), is(3128));
    }

    @Test
    public void httpProxyConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 80, mock(LayeringStrategy.class));

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "3128");

        Option<HttpHost> proxy = HttpClientProxyConfig.getProxy(scheme);

        assertThat(proxy.isDefined(), is(true));
        assertThat(proxy.get().getHostName(), is("localhost"));
        assertThat(proxy.get().getPort(), is(3128));
    }

    @Test
    public void httpProxyNotConfigured()
    {
        AsyncScheme scheme = new AsyncScheme("http", 80, mock(LayeringStrategy.class));

        Option<HttpHost> proxy = HttpClientProxyConfig.getProxy(scheme);

        assertThat(proxy.isEmpty(), is(true));
    }

    @Test
    public void proxyUserForHttpConfigured()
    {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("http.proxyUser", "user");
        System.setProperty("http.proxyPassword", "password");

        AbstractHttpAsyncClient client = mock(AbstractHttpAsyncClient.class);
        CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        when(client.getCredentialsProvider()).thenReturn(credentialsProvider);
        ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        ArgumentCaptor<AuthScope> scopeCaptor = ArgumentCaptor.forClass(AuthScope.class);

        HttpClientProxyConfig.applyProxyCredentials(client, AsyncSchemeRegistryFactory.createDefault());

        verify(credentialsProvider).setCredentials(scopeCaptor.capture(), credentialsCaptor.capture());

        assertThat(scopeCaptor.getValue().getHost(), is("localhost"));
        assertThat(scopeCaptor.getValue().getPort(), is(3128));
        assertThat(credentialsCaptor.getValue().getPassword(), is("password"));
        assertThat(credentialsCaptor.getValue().getUserPrincipal().getName(), is("user"));
        assertThat(scopeCaptor.getValue().getScheme(), equalToIgnoringCase("basic"));
    }

    @Test
    public void proxyUserForHttpsConfigured()
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("https.proxyUser", "user");
        System.setProperty("https.proxyPassword", "password");

        AbstractHttpAsyncClient client = mock(AbstractHttpAsyncClient.class);
        CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        when(client.getCredentialsProvider()).thenReturn(credentialsProvider);
        ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        ArgumentCaptor<AuthScope> scopeCaptor = ArgumentCaptor.forClass(AuthScope.class);

        HttpClientProxyConfig.applyProxyCredentials(client, AsyncSchemeRegistryFactory.createDefault());

        verify(credentialsProvider).setCredentials(scopeCaptor.capture(), credentialsCaptor.capture());

        assertThat(scopeCaptor.getValue().getHost(), is("localhost"));
        assertThat(scopeCaptor.getValue().getPort(), is(3128));
        assertThat(scopeCaptor.getValue().getScheme(), equalToIgnoringCase("basic"));
        assertThat(credentialsCaptor.getValue().getPassword(), is("password"));
        assertThat(credentialsCaptor.getValue().getUserPrincipal().getName(), is("user"));
    }

    @Test
    public void proxyAndProxyUserNotConfigured()
    {
        AbstractHttpAsyncClient client = mock(AbstractHttpAsyncClient.class);
        CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        when(client.getCredentialsProvider()).thenReturn(credentialsProvider);

        HttpClientProxyConfig.applyProxyCredentials(client, AsyncSchemeRegistryFactory.createDefault());
        verify(credentialsProvider, times(0)).setCredentials(any(AuthScope.class), any(Credentials.class));
    }

    @Test
    public void proxyUserNotConfigured()
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");

        AbstractHttpAsyncClient client = mock(AbstractHttpAsyncClient.class);
        CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        when(client.getCredentialsProvider()).thenReturn(credentialsProvider);

        verify(credentialsProvider, times(0)).setCredentials(any(AuthScope.class), any(Credentials.class));
    }

    @After
    public void tearDown()
    {
        System.setProperties(new Properties());
    }

}
