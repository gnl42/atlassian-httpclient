package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HostResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultHostResolver implements HostResolver {

    public static final HostResolver INSTANCE = new DefaultHostResolver();

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return SystemDefaultDnsResolver.INSTANCE.resolve(host);
    }
}
