package com.atlassian.webhooks.plugin.impl;

import com.atlassian.webhooks.plugin.WebHookRegistration;
import com.atlassian.webhooks.spi.provider.EventBuilder;
import com.atlassian.webhooks.spi.provider.MapperBuilder;

public final class EventBuilderImpl implements EventBuilder
{
    private final WebHookRegistration registration;

    public EventBuilderImpl(WebHookRegistration registration)
    {
        this.registration = registration;
    }

    @Override
    public <E> MapperBuilder<E> whenFired(Class<E> eventClass)
    {
        registration.setEventTrigger(eventClass);
        return new MapperBuilderImpl(registration);
    }
}
