package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface EventSerializer
{
    Object getEvent();
    String getJson() throws EventSerializationException;
}
