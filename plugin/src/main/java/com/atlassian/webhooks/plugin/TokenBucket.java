package com.atlassian.webhooks.plugin;

/**
 * Limits amount of log records per given amount of time to not flood log files
 */
class TokenBucket
{
    /**
     * Number of tokens to add to the bucket, <code>fillAmount</code> tokens are added to the bucket every
     * <code>fillInterval</code> seconds.
     */
    private final long fillAmount;

    /**
     * Interval at which to add tokens to the bucket, <code>fillAmount</code> tokens are added to the bucket every
     * <code>fillInterval</code> seconds.
     */
    private final long fillInterval;

    /**
     * The maximum number of tokens allowed in the bucket. This becomes the peak traffic burst allowed by the token
     * bucket in <code>fillInterval</code> seconds.
     */
    private final long maxTokens;

    private volatile long currentNumberOfTokens;

    private volatile long lastTokenRemovedTime;

    /**
     * Builds a token bucket
     * @param fillAmount how many token get regenerated per interval
     * @param fillInterval timout in seconds
     * @param maxTokens amount of tokens per interval
     */
    TokenBucket(long fillAmount, long fillInterval, long maxTokens)
    {
        this.fillAmount = fillAmount;
        this.fillInterval = fillInterval;
        this.maxTokens = maxTokens;
        this.currentNumberOfTokens = maxTokens;
        this.lastTokenRemovedTime = System.currentTimeMillis();
    }

    /**
     * Method to get a token from the bucket. If the bucket is not empty a token is removed.
     */
    public synchronized boolean getToken()
    {
        replaceTokens();
        boolean isEmpty = currentNumberOfTokens <= 0;

        if (!isEmpty)
        {
            currentNumberOfTokens--;
            lastTokenRemovedTime = System.currentTimeMillis();
        }

        return !isEmpty;
    }

    private void replaceTokens()
    {
        long currentTime = System.currentTimeMillis();
        long secondsSinceLastFill = (long) ((currentTime / 1000) - (lastTokenRemovedTime / 1000));

        if (secondsSinceLastFill >= fillInterval)
        {
            long numberOfTokensToAdd = (secondsSinceLastFill / fillInterval) * fillAmount;
            currentNumberOfTokens = currentNumberOfTokens + numberOfTokensToAdd > maxTokens ? maxTokens : currentNumberOfTokens + numberOfTokensToAdd;
        }

    }
}
