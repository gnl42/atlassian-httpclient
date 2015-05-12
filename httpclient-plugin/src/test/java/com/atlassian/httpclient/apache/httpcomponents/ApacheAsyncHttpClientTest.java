package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.Host;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.api.factory.ProxyOptions;
import com.atlassian.httpclient.api.factory.Scheme;

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
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public final class ApacheAsyncHttpClientTest
{
    private HttpsServer server;

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
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhe-client", options);
        httpClient.newRequest("https://aip-flyingagile.rhcloud.com").get().claim();
    }
    
    @Test 
    public void testWeCanTalkToRedhatWhenCiphersuitesAreSpecified()
    {
        String cipherSuites = "SSL_RSA_WITH_RC4_128_MD5, SSL_RSA_WITH_RC4_128_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_DES_CBC_SHA, SSL_DHE_RSA_WITH_DES_CBC_SHA, SSL_DHE_DSS_WITH_DES_CBC_SHA, SSL_RSA_EXPORT_WITH_RC4_40_MD5, SSL_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA, SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
        System.setProperty("https.cipherSuites", cipherSuites);
        HttpClientOptions options = new HttpClientOptions();
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
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhe-client", options);
        httpClient.newRequest("https://aip-flyingagile.rhcloud.com").get().claim();
        System.clearProperty("https.protocols");
    }

    @Test
    public void testWeCanTalkToGoogleWithoutDHE()
    {
        HttpClientOptions options = new HttpClientOptions();
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

    private static Iterable<String> ADD_ON_BASE_URLS = asList(
            "https://jira.agile-values.com",
            "https://aip-flyingagile.herokuapp.com",
            "https://announcer-for-confluence.aod-apps.wittified.com",
            "https://table-filter.herokuapp.com",
            "https://comalatech-canvas-production.herokuapp.com",
            "https://www.behave.pro",
            "https://simple-edit.agile-values.com",
            "https://issuechecklist-gebsun.rhcloud.com/",
            // "https://jemhcloud.thepluginpeople.com/app", Timeout in Browser and Tests
            "https://jira-integration.sifted.io",
            "https://ac.customercase.com",
            "https://svnpluginpro.matrixreq.com",
            "https://graphviz-confluence.addteq.com",
            "https://jira-work-calendar.com",
            "https://confluencegoogledrive.uswest.atlassian.io",
            "https://platform.harvestapp.com",
            "https://jira-plugin.wallsync.net",
            "https://similarissues.thestarware.com",
            "https://davidsimpson.me/labs/gister-for-confluence-cloud",
            "https://comalatech-canvas-production.herokuapp.com",
            "https://qtest.qtestnet.com",
            "https://tfs4jira-ondemand.spartez.com/tfs4jiraod",
            "https://store.yasoon.com/jira",
            // "https://www.jiraproject.com/ganttconnect", Timeout in Browser and Tests
            "https://jql.agile-values.com",
            "https://awl-gebsun.rhcloud.com",
            "https://atlas-addon.bugsio.com/ereminders",
            "https://bit-atlassian.herokuapp.com",
            "https://agilecards-ondemand.spartez.com/agilecardsod",
            "https://prod-play.zephyr4jiracloud.com/connect",
            "https://connect.jirareports.com",
            "https://upraise.cleverapps.io",
            "https://harvest-jira.usestrategery.com",
            "https://zcapp.herokuapp.com",
            "https://trackduck.s3.amazonaws.com/jira/prod/jira-plugin-1.0.jar",
            "https://ac-multiexcerpt-macro.herokuapp.com",
            "https://mscgen-confluence.addteq.com",
            "https://todo.stiltsoft.com",
            "https://www.lucidchart.com",
            // "https://ch-mibex-beautifulmath.herokuapp.com", Timeout in Browser and Tests
            "https://app.te52.com",
            "https://www.lucidchart.com",
            "https://s3.amazonaws.com/cql-search/",
            "https://jira.blrt.com",
            "https://appfire-addons.com/org.swift.confluence.cli",
            "https://my-reminders.useast.atlassian.io",
            "https://appfire-addons.com/org.swift.jira.cli",
            "https://copy-page-hierarchy.herokuapp.com",
            "https://rtasks-gebsun.rhcloud.com",
            "https://atlassian.wisoft.eu",
            "https://ps-jira-ac.herokuapp.com",
            "https://apps.dovico.net/jira",
            "https://simpletaskprinter.herokuapp.com",
            "https://bit-atlassian-organizer.herokuapp.com",
            "https://connect.assetsforjira.com",
            "https://xsd-viewer-cloud.herokuapp.com",
            "https://jira-quotes.herokuapp.com",
            "https://prod.practitest.com",
            "https://jira.abacolla.com/",
            "https://secure.reqtest.com/JiraIntegration",
            "https://www.pingmonit.com",
            "https://matrixjira-c.matrixreq.com",
            "https://hiptest.net",
            "https://reopening-counter.herokuapp.com",
            "https://webfrags.apps.wittified.com",
            "https://secure.donay.com/incentify-connect",
            "https://nh-ondemand.herokuapp.com",
            "https://matrixjira.matrixreq.com",
            "https://tpe.crowdsourcedtesting.com",
            "https://d27i9fmzbobp10.cloudfront.net/",
            "https://weekdone.com/jira",
            "https://customizer-jira.aod-apps.wittified.com",
            "https://aod.eazybi.com",
            "https://confluence-gmaps.firebaseapp.com/",
            "https://jiratimereports.herokuapp.com",
            "https://addon-anydo.firebaseapp.com",
            "https://addon-anydo.firebaseapp.com",
            "https://mscgen-jira.addteq.com",
            "https://jira-hipchat-discussions.herokuapp.com",
            "https://s3.amazonaws.com/bulkactiontools/",
            "https://lambda-plugin.herokuapp.com",
            "https://jiraplugin.zendesk.com/integrations/jira",
            "https://vast-caverns-9249.herokuapp.com",
            "https://d3uu992mgfv1d.cloudfront.net",
            "https://desolate-beach-6557.herokuapp.com/",
            "https://autowatch.herokuapp.com",
            "https://connect-usage-statistics.herokuapp.com",
            "https://ac-relatedissues.herokuapp.com",
            "https://embedly-addon.herokuapp.com",
            "https://ac-rsvp.herokuapp.com",
            "https://jira.flapps.com",
            "https://secure.aha.io",
            "https://inspire-confluence-app.herokuapp.com",
            "https://ac-copy-space.herokuapp.com",
            "https://buffalo.arvixe.com",
            "https://create-your-own.play-sql.com",
            "https://learn-connect.herokuapp.com",
            "https://whoslooking.herokuapp.com",
            "https://com-playsql-sqlsaas.herokuapp.com",
            "https://www.cloudflare.com"
    );

    @Test
    public void testWeCanTalkToAllPublicAddonsWithProxy()
    {
        Map<String, Throwable> errors = new HashMap<String, Throwable>();

        for (String addOnBaseUrl: ADD_ON_BASE_URLS)
        {
            try
            {
                testWeCanTalkToSNIAddonswithProxy(addOnBaseUrl);
            }
            catch (Throwable t)
            {
                errors.put(addOnBaseUrl, t);
            }
        }

        for (Map.Entry<String, Throwable> error: errors.entrySet())
        {
            System.err.printf("%s: ", error.getKey());
            error.getValue().printStackTrace(System.err);
            System.err.println();
        }

        assertThat(String.format("Contacting add-on base URLs failed in at least one case. Check the log for details. Num failures = %d.", errors.size()),
                errors.size(), is(0));
    }

    @Test
    public void testWeCanTalkToSNIAddons_scrumdash()
    {
        HttpClientOptions options = new HttpClientOptions();
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhes-client", options);
        httpClient.newRequest("https://cloudflare.com").get().claim();
    }

    private void testWeCanTalkToSNIAddonswithProxy(String uri)
    {
        Host proxyHost = new Host("localhost", 8888);
        HttpClientOptions options = new HttpClientOptions();
        options.setProxyOptions(ProxyOptions.ProxyOptionsBuilder.create().withProxy(Scheme.HTTP, proxyHost).withProxy(Scheme.HTTPS, proxyHost).build());
        final HttpClient httpClient = new ApacheAsyncHttpClient<Void>("non-dhes-client", options);
        httpClient.newRequest(uri).get().claim();
    }
    */
}
