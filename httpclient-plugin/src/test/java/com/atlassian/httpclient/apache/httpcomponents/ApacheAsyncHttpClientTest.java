package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.api.factory.ProxyOptions;
import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class ApacheAsyncHttpClientTest
{
    private HttpsServer server;

    private List<String> dheDisabledHosts = Lists.newArrayList("aip-flyingagile.rhcloud.com", "www.google.com");

    @Before
    public void setUp() throws Exception
    {
        server = HttpsServer.create(new InetSocketAddress(8000), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(getSslContext()));
        server.createContext("/", new NoOpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @After
    public void tearDown() throws Exception
    {
        if (server != null)
        {
            server.stop(3);
        }
    }

    //These tests are probably not something we want to keep because they rely on
    //external services, but I'm including them here since I can't figure out
    //any other way to test the stuff (without unreasonable effort, such us spinning up 
    //multiple servers running different JVM versions)
    @Test 
    public void testWeCanTalkToRedHatWithoutDHE()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhe-client", options);
        httpClient.newRequest("https://aip-flyingagile.rhcloud.com").get().claim();
    }
    
    @Test 
    public void testWeCanTalkToRedhatWhenCiphersuitesAreSpecified()
    {
        String cipherSuites = "SSL_RSA_WITH_RC4_128_MD5, SSL_RSA_WITH_RC4_128_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_DES_CBC_SHA, SSL_DHE_RSA_WITH_DES_CBC_SHA, SSL_DHE_DSS_WITH_DES_CBC_SHA, SSL_RSA_EXPORT_WITH_RC4_40_MD5, SSL_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
        System.setProperty("https.cipherSuites", cipherSuites);
        HttpClientOptions options = new HttpClientOptions();
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhe-client", options);
        httpClient.newRequest("https://aip-flyingagile.rhcloud.com").get().claim();
        System.clearProperty("https.cipherSuites");
    }
    
    @Test
    public void testWeCanTalkToRedhatWhenProtocolsAreSpecified()
    {
        String protocols = "SSLv2Hello, SSLv3, TLSv1";
        System.setProperty("https.protocols", protocols);
        HttpClientOptions options = new HttpClientOptions();
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhe-client", options);
        httpClient.newRequest("https://aip-flyingagile.rhcloud.com").get().claim();
        System.clearProperty("https.protocols");
    }

    @Test
    public void testWeCanTalkToGoogleWithoutDHE()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhes-client", options);
        httpClient.newRequest("https://www.google.com").get().claim();
    }

    @Test
    public void testGetWhenNotTrustingSelfSignedCertificates()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(false);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("not-trusty-client", options);
        try
        {
            httpClient.newRequest("https://localhost:8000/").get().claim();
        }
        catch (RuntimeException expected)
        {
            assertEquals(SSLHandshakeException.class, expected.getCause().getClass());
        }
    }

    @Test
    public void testGetWhenTrustingSelfSignedCertificates()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(true);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("trusty-client", options);
        httpClient.newRequest("https://localhost:8000/").get().claim();
    }

    @Test
    public void testGetWithoutAProxy()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setProxyOptions(ProxyOptions.ProxyOptionsBuilder.create().withNoProxy().build());
        options.setTrustSelfSignedCertificates(true);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("trusty-client", options);
        httpClient.newRequest("https://localhost:8000/").get().claim();
    }

    private SSLContext getSslContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException
    {
        char[] passphrase = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(this.getClass().getResourceAsStream("/keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    private final class NoOpHandler implements HttpHandler
    {
        public void handle(HttpExchange t) throws IOException
        {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /*
    // Uncomment the tests below and run a local proxy (such as tinyproxy) to test SNI with a proxy.
    // They're commented out because relying on external services in automated tests is a bad plan.
    // These tests should go away when we no longer need to use SniCompatibleSSLIOSessionStrategy.

    @Test
    public void testWeCanTalkToSNIAddons_dlsstudios()
    {
        testWeCanTalkToSNIAddonswithProxy("https://jira.dlsstudios.com/");
    }

    @Test
    public void testWeCanTalkToSNIAddonswithProxy_stiltsoft()
    {
        testWeCanTalkToSNIAddonswithProxy("https://todo.stiltsoft.com");
    }

    @Test
    public void testWeCanTalkToSNIAddonswithProxy_scrumdash()
    {
       testWeCanTalkToSNIAddonswithProxy("https://scrumdash.com/");
    }

    @Test
    public void testWeCanTalkToSNIAddonswithProxy_DavidBlack()
    {
        testWeCanTalkToSNIAddonswithProxy("https://d1b.org");
    }

    @Test
    public void testWeCanTalkToSNIAddons_scrumdash()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhes-client", options);
        httpClient.newRequest("https://scrumdash.com/").get().claim();
    }

    private void testWeCanTalkToSNIAddonswithProxy(String uri)
    {
        Host proxyHost = new Host("localhost", 3128);
        HttpClientOptions options = new HttpClientOptions();
        options.setProxyOptions(ProxyOptions.ProxyOptionsBuilder.create().withProxy(Scheme.HTTP, proxyHost).withProxy(Scheme.HTTPS, proxyHost).build());
        options.setDheDisabledHosts(dheDisabledHosts);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhes-client", options);
        httpClient.newRequest(uri).get().claim();
    }
    */
}
