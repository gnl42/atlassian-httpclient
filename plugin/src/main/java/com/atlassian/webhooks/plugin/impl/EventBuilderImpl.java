package com.atlassian.webhooks.plugin.impl;

import com.atlassian.webhooks.plugin.WebHookRegistration;
import com.atlassian.webhooks.spi.provider.EventBuilder;
import com.atlassian.webhooks.spi.provider.MapperBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EventBuilderImpl implements EventBuilder
{
    private final WebHookRegistration registration;

    public EventBuilderImpl(WebHookRegistration registration)
    {
        this.registration = checkNotNull(registration);
    }

    @Override
    public <E> MapperBuilder<E> whenFired(Class<E> eventClass)
    {
        return new MapperBuilderImpl<E>(registration.setEventTrigger(eventClass));
    }
}
