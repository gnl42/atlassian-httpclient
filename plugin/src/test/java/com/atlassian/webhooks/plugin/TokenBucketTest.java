package com.atlassian.webhooks.plugin;

import junit.framework.Assert;
import org.junit.Test;

public class TokenBucketTest {

    public static final int TEN_MINUTES = 600;

    @Test
    public void testGetToken() throws Exception {

        TokenBucket logMessageRateLimiter = new TokenBucket(1, TEN_MINUTES, 5);

        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());

        Assert.assertFalse("Token bucket have to be exhausted", logMessageRateLimiter.getToken());
    }
}
