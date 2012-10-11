package com.atlassian.webhooks.spi.provider;

/**
 * Creates event serializers for an event type
 */
public interface EventSerializerFactory<T>
{
    EventSerializer create(T event);
}
