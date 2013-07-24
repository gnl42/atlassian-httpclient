package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public final class DefaultConstructorConstructionStrategy implements ConstructionStrategy
{
    @Override
    public <T> T get(Class<T> type)
    {
        try
        {
            return Preconditions.checkNotNull(type).newInstance();
        }
        catch (InstantiationException e)
        {
            throw Throwables.propagate(e);
        }
        catch (IllegalAccessException e)
        {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public EventMatcher<Object> getEventMatcher(final Class<? extends EventMatcher> eventMatcherClass, final Object event)
    {
        if (eventMatcherClass.equals(EventMatcher.EventClassEventMatcher.class))
        {
            return new EventMatcher.EventClassEventMatcher(event.getClass());
        }
        else
        {
            return get(eventMatcherClass);
        }
    }
}
