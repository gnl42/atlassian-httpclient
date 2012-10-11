package com.atlassian.webhooks.spi.provider;

/**
 *
 */
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
