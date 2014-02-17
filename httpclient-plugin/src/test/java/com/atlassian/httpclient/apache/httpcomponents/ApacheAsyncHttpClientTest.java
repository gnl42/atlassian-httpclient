package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.junit.http.HttpUtils;
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

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public final class ApacheAsyncHttpClientTest
{
    private static final int DEFAULT_PORT = 8000;
    private HttpsServer server;
    private int port;

    @Before
    public void setUp() throws Exception
    {
        final int requestedPort = Integer.valueOf(System.getProperty("http.port", String.valueOf(DEFAULT_PORT)));
        port = HttpUtils.pickFreePort(requestedPort);
        System.out.println("************************** : " + port);
        server = HttpsServer.create(new InetSocketAddress(port), 0);
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

    @Test
    public void testGetWhenNotTrustingSelfSignedCertificates()
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(false);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("not-trusty-client", options);
        try
        {
            httpClient.newRequest(getServerUrl()).get().claim();
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
        httpClient.newRequest(getServerUrl()).get().claim();
    }

    private String getServerUrl()
    {
        return format("https://localhost:%s/", port);
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
}
