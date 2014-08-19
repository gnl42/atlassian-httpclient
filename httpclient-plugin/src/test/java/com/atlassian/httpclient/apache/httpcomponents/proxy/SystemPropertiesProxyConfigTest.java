package com.atlassian.httpclient.apache.httpcomponents.proxy;

import com.atlassian.fugue.Option;
import com.google.common.collect.Iterables;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SystemPropertiesProxyConfigTest
{
    @Before
    public void setUp()
    {
        System.setProperties(new Properties());
    }

    @Test
    public void httpsProxyConfigured()
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");
        ProxyConfig config = new SystemPropertiesProxyConfig();
        Option<HttpHost> proxy = config.getProxyHost();

        assertThat(proxy.isDefined(), is(true));
        assertThat(proxy.get().getHostName(), is("localhost"));
        assertThat(proxy.get().getPort(), is(3128));
    }

    @Test
    public void httpProxyConfigured()
    {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "3128");
        ProxyConfig config = new SystemPropertiesProxyConfig();
        Option<HttpHost> proxy = config.getProxyHost();

        assertThat(proxy.isDefined(), is(true));
        assertThat(proxy.get().getHostName(), is("localhost"));
        assertThat(proxy.get().getPort(), is(3128));
    }

    @Test
    public void httpProxyNotConfigured()
    {
        ProxyConfig config = new SystemPropertiesProxyConfig();
        Option<HttpHost> proxy = config.getProxyHost();

        assertThat(proxy.isEmpty(), is(true));
    }

    @Test
    public void proxyUserForHttpConfigured()
    {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("http.proxyUser", "user");
        System.setProperty("http.proxyPassword", "password");

        ProxyConfig config = new SystemPropertiesProxyConfig();
        Iterable<ProxyConfig.AuthenticationInfo> authenticationInfos = config.getAuthenticationInfo();

        assertThat(authenticationInfos, Matchers.<ProxyConfig.AuthenticationInfo>iterableWithSize(1));

        final ProxyConfig.AuthenticationInfo authenticationInfo = Iterables.getOnlyElement(authenticationInfos);

        assertThat(authenticationInfo.getCredentials().isDefined(), is(true));
        assertThat(authenticationInfo.getCredentials().get(), instanceOf(UsernamePasswordCredentials.class));

        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) authenticationInfo.getCredentials().get();

        assertThat(credentials.getPassword(), is("password"));
        assertThat(credentials.getUserName(), is("user"));
    }

    @Test
    public void proxyUserForHttpsConfigured()
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("https.proxyUser", "user");
        System.setProperty("https.proxyPassword", "password");

        ProxyConfig config = new SystemPropertiesProxyConfig();
        Iterable<ProxyConfig.AuthenticationInfo> authenticationInfos = config.getAuthenticationInfo();

        assertThat(authenticationInfos, Matchers.<ProxyConfig.AuthenticationInfo>iterableWithSize(1));

        final ProxyConfig.AuthenticationInfo authenticationInfo = Iterables.getOnlyElement(authenticationInfos);

        assertThat(authenticationInfo.getCredentials().isDefined(), is(true));
        assertThat(authenticationInfo.getCredentials().get(), instanceOf(UsernamePasswordCredentials.class));

        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) authenticationInfo.getCredentials().get();

        assertThat(credentials.getPassword(), is("password"));
        assertThat(credentials.getUserName(), is("user"));
    }

    @Test
    public void proxyAndProxyUserNotConfigured()
    {
        ProxyConfig config = new SystemPropertiesProxyConfig();
        final Iterable<ProxyConfig.AuthenticationInfo> authenticationInfo = config.getAuthenticationInfo();

        assertThat(authenticationInfo, Matchers.<ProxyConfig.AuthenticationInfo>iterableWithSize(0));

        final Option<HttpHost> proxyHost = config.getProxyHost();

        assertThat(proxyHost.isDefined(), Matchers.is(false));
    }

    @Test
    public void proxyUserNotConfigured()
    {
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "3128");

        ProxyConfig config = new SystemPropertiesProxyConfig();

        final Iterable<ProxyConfig.AuthenticationInfo> authenticationInfos = config.getAuthenticationInfo();
        assertThat(authenticationInfos, Matchers.<ProxyConfig.AuthenticationInfo>iterableWithSize(1));

        final ProxyConfig.AuthenticationInfo authenticationInfo = Iterables.getOnlyElement(authenticationInfos);
        assertThat(authenticationInfo.getCredentials().isDefined(), Matchers.is(false));

        final Option<HttpHost> proxyHost = config.getProxyHost();
        assertThat(proxyHost.isDefined(), Matchers.is(true));
    }
}
