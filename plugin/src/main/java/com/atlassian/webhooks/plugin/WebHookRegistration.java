package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;

/**
 * A registration of a web hook
 */
public final class WebHookRegistration
{
    private final String id;
    private Class<?> eventClass;
    private EventSerializerFactory eventSerializerFactory;
    private EventMatcher eventMatcher;

    public WebHookRegistration(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public WebHookRegistration setEventTrigger(Class<?> eventClass)
    {
        this.eventClass = eventClass;
        this.eventMatcher = new EventMatcher.EventClassEventMatcher(eventClass);
        return this;
    }

    public WebHookRegistration setEventSerializerFactory(EventSerializerFactory<?> eventSerializerFactory)
    {
        this.eventSerializerFactory = eventSerializerFactory;
        return this;
    }

    public EventSerializer getEventSerializer(Object event)
    {
        return eventSerializerFactory.create(event);
    }

    public Class<?> getEventClass()
    {
        return eventClass;
    }

    public WebHookRegistration setEventMatcher(EventMatcher<?> eventMatcher)
    {
        this.eventMatcher = eventMatcher;
        return this;
    }

    public EventMatcher getEventMatcher()
    {
        return eventMatcher;
    }
}
