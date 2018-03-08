package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.BannedHostException;
import com.atlassian.httpclient.api.HostResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Restricted host resolver can be used to restrict the IPs that a host may resolve to. When utilised in a http client
 * each call will at some point will pass the endpoint to connect to through the {@link #resolve(String)} method.
 * <p>
 * This implementation will throw an exception if it receives a host that resolves to an IP in the restricted list.
 * <p>
 * Do note, that even if a specific IP is to be connected to, the IP will still be resolved through this class and
 * eventually be blocked if required.
 */
public class RestrictedHostResolver implements HostResolver {

    private final List<IpAddressMatcher> cidrs;

    /**
     * @param restrictedCIDRs list of cidrs to block from resolving
     */
    public RestrictedHostResolver(List<String> restrictedCIDRs) {
        cidrs = restrictedCIDRs.stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return restrict(DefaultHostResolver.INSTANCE.resolve(host));
    }

    private InetAddress[] restrict(InetAddress[] addresses) throws UnknownHostException {
        for (IpAddressMatcher cidr : cidrs) {
            for (InetAddress address : addresses) {
                String hostAddress = address.getHostAddress();
                if (cidr.matches(hostAddress)) {
                    throw new BannedHostException("This host has been blocked for access");
                }
            }
        }
        return addresses;
    }
}
