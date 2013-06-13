package com.atlassian.webhooks.plugin.test;

/**
 */
public class EventWithPersistentListener
{
    private final String qualificator;
    private final String secondaryKey;

    public EventWithPersistentListener(final String qualifactor, final String secondaryKey)
    {
        this.qualificator = qualifactor;
        this.secondaryKey = secondaryKey;
    }

    public String getQualificator()
    {
        return qualificator;
    }

    public String getSecondaryKey()
    {
        return secondaryKey;
    }
}
