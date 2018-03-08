package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HostResolver;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.fail;

public class RestrictedHostResolverTest {

    private static final String AWS_META_HOST = "169.254.169.254";
    private final Matcher<String> BLOCKED_HOST_MATCHER = containsString("This host has been blocked for access");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBlockSingleIP() throws UnknownHostException {
        HostResolver restrictedHostResolver = new RestrictedHostResolver(ImmutableList.of(AWS_META_HOST + "/32"));

        expectedException.expectMessage(BLOCKED_HOST_MATCHER);
        restrictedHostResolver.resolve(AWS_META_HOST);
    }

    @Test
    public void testIpv6AddressesCanBeBlocked() {
        ImmutableList<String> banList = ImmutableList.of("fd12:3456:7890:1423:ffff:ffff:ffff:ffff", "0:0:0:0:0:ffff:808:808");
        HostResolver resolver = new RestrictedHostResolver(banList);

        assertAllIpsFail(banList, resolver);
    }

    @Test
    public void testListRestricted() {
        ImmutableList<String> banList = ImmutableList.of("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4");
        HostResolver resolver = new RestrictedHostResolver(banList);

        assertAllIpsFail(banList, resolver);
    }

    @Test
    public void testNoAddressesBlocked() throws UnknownHostException {
        HostResolver restrictedHostResolver = new RestrictedHostResolver(new ArrayList<>());
        restrictedHostResolver.resolve(AWS_META_HOST);
    }

    @Test
    public void testSmallRange() throws UnknownHostException {
        HostResolver restrictedHostResolver = new RestrictedHostResolver(ImmutableList.of("192.168.0.1/30"));

        restrictedHostResolver.resolve("192.168.0.4");

        expectedException.expectMessage(BLOCKED_HOST_MATCHER);
        restrictedHostResolver.resolve("192.168.0.2");
    }

    private void assertAllIpsFail(ImmutableList<String> banList, HostResolver resolver) {
        for (String ip : banList) {
            try {
                resolver.resolve(ip);
                fail("Resolution should fail for ip " + ip);
            } catch (UnknownHostException ignored) {
            }
        }
    }

}