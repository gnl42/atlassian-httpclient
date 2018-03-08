package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.BannedHostException;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.api.factory.ProxyOptions;
import com.google.common.collect.ImmutableList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;

public final class ApacheAsyncHttpClientTest {
    private HttpsServer server;

    private int port;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        server = HttpsServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.setHttpsConfigurator(new HttpsConfigurator(getSslContext()));
        server.createContext("/", new NoOpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop(3);
        }
    }

    @Test
    public void testGetWhenNotTrustingSelfSignedCertificates() {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(false);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("not-trusty-client", options);
        try {
            httpClient.newRequest(serverUrl()).get().claim();
        } catch (RuntimeException expected) {
            assertEquals(SSLHandshakeException.class, expected.getCause().getClass());
        }
    }

    @Test
    public void testGoingToAWSEndpoint() {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(true);
        options.setBlacklistedAddresses(ImmutableList.of("169.0.0.0/8"));

        final HttpClient client = new ApacheAsyncHttpClient<>("trust-client", options);

        // a valid request should work
        client.newRequest(serverUrl()).get().claim();
        expectedException.expectCause(isA(BannedHostException.class));
        client.newRequest("http://169.254.169.254").get().claim();
    }

    @Test
    public void testGetWhenTrustingSelfSignedCertificates() {
        HttpClientOptions options = new HttpClientOptions();
        options.setTrustSelfSignedCertificates(true);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("trusty-client", options);
        httpClient.newRequest(serverUrl()).get().claim();
    }

    @Test
    public void testGetWithoutAProxy() {
        HttpClientOptions options = new HttpClientOptions();
        options.setProxyOptions(ProxyOptions.ProxyOptionsBuilder.create().withNoProxy().build());
        options.setTrustSelfSignedCertificates(true);
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("trusty-client", options);
        httpClient.newRequest(serverUrl()).get().claim();
    }

    private SSLContext getSslContext() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
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

    private final class NoOpHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private String serverUrl() {
        return String.format("https://localhost:%d/", port);
    }
}
