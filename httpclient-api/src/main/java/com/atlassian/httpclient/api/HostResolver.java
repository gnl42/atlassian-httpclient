package com.atlassian.httpclient.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves ip addresses based on a host
 */
public interface HostResolver {

    /**
     * Get an array of IP addresses for a host
     * @param host host to get ip for. Can just be an ip itself
     * @return resolved addresses for the host
     * @throws UnknownHostException if the host cannot be found, or {@link BannedHostException} if the ip is blacklisted
     */
    InetAddress[] resolve(String host) throws UnknownHostException;
}

