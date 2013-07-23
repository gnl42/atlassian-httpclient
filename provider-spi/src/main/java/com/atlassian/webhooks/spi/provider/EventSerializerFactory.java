package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

/**
 * Creates event serializers for an event type
 */
@PublicSpi
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
