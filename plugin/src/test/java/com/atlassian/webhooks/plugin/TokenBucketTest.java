package com.atlassian.webhooks.plugin;

import junit.framework.Assert;
import org.junit.Test;

public class TokenBucketTest
{
    private static final int THE_BEGINNING_OF_TIME = 0;
    private static final int ONE_SECOND = 1000;

    private static final int TEN_MINUTES = 10 * 60 * 1000;

    @Test
    public void testGetTokenBecomesExhausted() throws Exception
    {

        TokenBucket logMessageRateLimiter = new TokenBucket(1, TEN_MINUTES, 5);

        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());

        Assert.assertFalse("Token bucket have to be exhausted", logMessageRateLimiter.getToken());
    }

    @Test
    public void testGetTokenWithTokenRecovery() throws Exception
    {

        MyTokenBucket logMessageRateLimiter = new MyTokenBucket(1, ONE_SECOND, 5);
        logMessageRateLimiter.setCurrentMillis(THE_BEGINNING_OF_TIME);

        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());
        Assert.assertTrue(logMessageRateLimiter.getToken());

        Assert.assertFalse("Token bucket have to be exhausted", logMessageRateLimiter.getToken());

        // Recovering exact 1 token
        logMessageRateLimiter.setCurrentMillis(ONE_SECOND);
        Assert.assertTrue("One token has to be recovered after a second", logMessageRateLimiter.getToken());
        Assert.assertFalse("Token bucket have to be exhausted", logMessageRateLimiter.getToken());

        // Recovering all 5 tokens (5 + 1 seconds = 6 seconds)
        logMessageRateLimiter.setCurrentMillis(6 * ONE_SECOND);
        Assert.assertTrue("5 tokens has to be recovered in 5 seconds period, 5 left", logMessageRateLimiter.getToken());
        Assert.assertTrue("5 tokens has to be recovered in 5 seconds period, 4 left", logMessageRateLimiter.getToken());
        Assert.assertTrue("5 tokens has to be recovered in 5 seconds period, 3 left", logMessageRateLimiter.getToken());
        Assert.assertTrue("5 tokens has to be recovered in 5 seconds period, 2 left", logMessageRateLimiter.getToken());
        Assert.assertTrue("5 tokens has to be recovered in 5 seconds period, 1 left", logMessageRateLimiter.getToken());
        Assert.assertFalse("Token bucket have to be exhausted", logMessageRateLimiter.getToken());

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class MyTokenBucket extends TokenBucket
    {

        private long currentMillis;

        MyTokenBucket(final long fillAmount, final long fillInterval, final long maxTokens)
        {
            super(fillAmount, fillInterval, maxTokens);
        }

        private void setCurrentMillis(final long currentMillis)
        {
            this.currentMillis = currentMillis;
        }

        @Override
        long getCurrentMillis()
        {
            return currentMillis;
        }
    }
}
