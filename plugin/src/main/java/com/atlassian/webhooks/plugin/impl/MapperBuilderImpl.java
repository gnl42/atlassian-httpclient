package com.atlassian.webhooks.plugin.impl;

import com.atlassian.webhooks.plugin.WebHookRegistration;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.MapperBuilder;

import static com.google.common.base.Preconditions.*;

public class MapperBuilderImpl<E> implements MapperBuilder<E>
{
    private final WebHookRegistration registration;

    public MapperBuilderImpl(WebHookRegistration registration)
    {
        this.registration = checkNotNull(registration);
    }

    @Override
    public void serializedWith(EventSerializerFactory eventSerializerFactory)
    {
        registration.setEventSerializerFactory(eventSerializerFactory);
    }

    @Override
    public MapperBuilder<E> matchedBy(EventMatcher eventTypeMatcher)
    {
        registration.setEventMatcher(eventTypeMatcher);
        return this;
    }
}
