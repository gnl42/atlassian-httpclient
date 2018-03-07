package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Resolver;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;

public class RestrictedResolverTest {

    private static final String AWS_META_HOST = "169.254.169.254";
    private final Matcher<String> BLOCKED_HOST_MATCHER = containsString("This host has been blocked for access");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoAddressesBlocked() throws UnknownHostException {
        Resolver restrictedResolver = new RestrictedResolver(new ArrayList<>());
        restrictedResolver.resolve(AWS_META_HOST);
    }

    @Test
    public void testBlockSingleIP() throws UnknownHostException {
        Resolver restrictedResolver = new RestrictedResolver(ImmutableList.of(AWS_META_HOST + "/32"));

        expectedException.expectMessage(BLOCKED_HOST_MATCHER);
        restrictedResolver.resolve(AWS_META_HOST);
    }

    @Test
    public void testSmallRange() throws UnknownHostException {
        Resolver restrictedResolver = new RestrictedResolver(ImmutableList.of("192.168.0.1/30"));

        restrictedResolver.resolve("192.168.0.4");

        expectedException.expectMessage(BLOCKED_HOST_MATCHER);
        restrictedResolver.resolve("192.168.0.2");
    }

}