package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Resolver;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class RestrictedResolver implements Resolver {

    private final List<IpAddressMatcher> cidrs;

    /**
     *
     * @param restrictedCIDRs list of cidrs to block from resolving
     */
    public RestrictedResolver(List<String> restrictedCIDRs) {
        cidrs = restrictedCIDRs.stream()
                .map(IpAddressMatcher::new)
                .collect(Collectors.toList());
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return restrict(DefaultResolver.INSTANCE.resolve(host));
    }

    private InetAddress[] restrict(InetAddress[] addresses) throws UnknownHostException {
        for (IpAddressMatcher cidr : cidrs) {
            for (InetAddress address : addresses) {
                if (address instanceof Inet4Address) {
                    String hostAddress = address.getHostAddress();
                    if (cidr.matches(hostAddress)) {
                        throw new UnknownHostException("This host has been blocked for access");
                    }
                }
            }
        }
        return addresses;
    }
}
