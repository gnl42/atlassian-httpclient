package com.atlassian.httpclient.apache.httpcomponents;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;

// Enable us to make outbound connections to servers using SNI when we're using a proxy server (and we are always using a proxy in Cloud).
// Remove this class when we are using a version of org.apache.httpcomponents:httpasyncclient that includes the fix <https://issues.apache.org/jira/browse/HTTPASYNC-90>.
// Version 4.0.2 suffers from this bug.
// This class is a copy of the necessary parts of SSLIOSessionStrategy except for the 'host' bug fix below.
public class SniCompatibleSSLIOSessionStrategy extends SSLIOSessionStrategy
{
    private final SSLContext sslContext;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;

    public SniCompatibleSSLIOSessionStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier)
    {
        super(sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
        this.sslContext = Args.notNull(sslContext, "SSL context");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
    }

    public SSLIOSession upgrade(final HttpHost host, final IOSession iosession) throws IOException
    {
        Asserts.check(!(iosession instanceof SSLIOSession), "I/O session is already upgraded to TLS/SSL");
        final SSLIOSession ssliosession = new SSLIOSession(
                iosession,
                SSLMode.CLIENT,
                host, // this line is the whole reason for this subclass; see the apache bug https://issues.apache.org/jira/browse/HTTPASYNC-90
                this.sslContext,
                new SSLSetupHandler() {

                    public void initalize(
                            final SSLEngine sslengine) throws SSLException
                    {
                        if (supportedProtocols != null) {
                            sslengine.setEnabledProtocols(supportedProtocols);
                        }
                        if (supportedCipherSuites != null) {
                            sslengine.setEnabledCipherSuites(supportedCipherSuites);
                        }
                        initializeEngine(sslengine);
                    }

                    public void verify(
                            final IOSession iosession,
                            final SSLSession sslsession) throws SSLException {
                        verifySession(host, iosession, sslsession);
                    }

                });
        iosession.setAttribute(SSLIOSession.SESSION_KEY, ssliosession);
        ssliosession.initialize();
        return ssliosession;
    }
}
