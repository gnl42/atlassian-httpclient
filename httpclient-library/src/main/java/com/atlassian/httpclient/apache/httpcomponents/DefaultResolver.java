package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Resolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultResolver implements Resolver {

    public static final Resolver INSTANCE = new DefaultResolver();

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return SystemDefaultDnsResolver.INSTANCE.resolve(host);
    }
}
