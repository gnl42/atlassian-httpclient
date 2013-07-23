package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface MapperBuilder<E>
{
    void serializedWith(EventSerializerFactory eventSerializerFactory);

    MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher);
}
