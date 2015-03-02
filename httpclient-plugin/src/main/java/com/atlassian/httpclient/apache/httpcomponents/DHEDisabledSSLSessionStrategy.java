package com.atlassian.httpclient.apache.httpcomponents;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Workaround for http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6521495
 * 
 * See https://ecosystem.atlassian.net/browse/AC-1424 for more details
 * 
 * This class disables SSL ciphers using Diffie-Hellmann key exchange when connecting to any of the blacklisted hosts.
 * Specifically, we have to disable DHE for *.rhcloud.com, since they have setup a dh_param with length of 2048.
 * 
 * The code should be deleted once Ondemand is updated to JDK8.
 * 
 * 
 */
public class DHEDisabledSSLSessionStrategy extends SSLIOSessionStrategy
{

    private static final String DHE_CIPHER_PREFIX = "TLS_DHE";
    private final Iterable<String> hostBlacklist;
    private final SSLContext sslContext;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String[] httpsCipherSuites;
    private final String[] httpsProtocols;

    private final class NonDHESSLSetupHandler implements SSLSetupHandler
    {
        private final HttpHost host;

        private NonDHESSLSetupHandler(HttpHost host)
        {
            this.host = host;
        }

        public void initalize(final SSLEngine sslEngine) throws SSLException
        {
            if (Iterables.contains(hostBlacklist, host.getHostName()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Disabling dhe for host: " + host.getHostName());
                }
                sslEngine.setEnabledCipherSuites(getNonDHECiphers());
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Enabling DHE for host: " + host.getHostName());
                }
                sslEngine.setEnabledCipherSuites(httpsCipherSuites);
            }
            sslEngine.setEnabledProtocols(httpsProtocols);
            initializeEngine(sslEngine);
        }

        public void verify(final IOSession iosession, final SSLSession sslsession) throws SSLException
        {
            verifySession(host, iosession, sslsession);
        }
    }

    public DHEDisabledSSLSessionStrategy(SSLContext sslContext, List<String> hostBlacklist,
        @Nullable String[] httpsProtocols, @Nullable String[] httpsCipherSuites, X509HostnameVerifier hostnameVerifier)
    {
        super(sslContext, httpsProtocols, httpsCipherSuites, hostnameVerifier);
        this.sslContext = Preconditions.checkNotNull(sslContext, "SSL context");

        SSLParameters sslParams = sslContext.getDefaultSSLParameters();

        Preconditions.checkNotNull(hostnameVerifier);
        this.hostBlacklist = Preconditions.checkNotNull(hostBlacklist);
        if (!(httpsProtocols == null))
        {
            this.httpsProtocols = httpsProtocols;
        }
        else
        {
            this.httpsProtocols = sslParams.getProtocols();
        }
        if (!(httpsCipherSuites == null))
        {
            this.httpsCipherSuites = httpsCipherSuites;
        }
        else
        {
            this.httpsCipherSuites = sslParams.getCipherSuites();
        }
    }

    public DHEDisabledSSLSessionStrategy(SSLContext sslContext)
    {
        super(sslContext);
        this.sslContext = sslContext;
        SSLParameters sslParams = sslContext.getDefaultSSLParameters();
        this.hostBlacklist = Lists.newArrayList();
        this.httpsCipherSuites = sslParams.getCipherSuites();
        this.httpsProtocols = sslParams.getProtocols();
    }

    @Override
    public SSLIOSession upgrade(final HttpHost host, final IOSession ioSession) throws IOException
    {
        final SSLIOSession sslIOSession = new SSLIOSession(ioSession,
                                                           SSLMode.CLIENT,
                                                           host,
                                                           this.sslContext,
                                                           new NonDHESSLSetupHandler(host));
        ioSession.setAttribute(SSLIOSession.SESSION_KEY, sslIOSession);
        sslIOSession.initialize();
        return sslIOSession;
    }

    private String[] getNonDHECiphers()
    {
        List<String> ciphers = Arrays.asList(this.httpsCipherSuites);

        Iterable<String> nonDHECiphers = Iterables.filter(ciphers, new Predicate<String>()
        {
            @Override
            public boolean apply(String input)
            {
                return !input.startsWith(DHE_CIPHER_PREFIX);
            }
        });
        String[] nonDHECipherSuites = Iterables.toArray(nonDHECiphers, String.class);
        return nonDHECipherSuites;
    }
}