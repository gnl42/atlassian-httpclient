package com.atlassian.webhooks.plugin.test;

import com.atlassian.event.api.EventPublisher;

import static com.google.common.base.Preconditions.*;

public final class ServiceAccessor
{
    public static EventPublisher eventPublisher;

    public ServiceAccessor(EventPublisher eventPublisher)
    {
        ServiceAccessor.eventPublisher = checkNotNull(eventPublisher);
    }
}
