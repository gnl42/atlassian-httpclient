package com.atlassian.webhooks.spi.provider;

public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher);
}
