package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventMatcher;

public interface ConstructionStrategy
{
    <T> T get(Class<T> type);

    EventMatcher<Object> getEventMatcher(Class<? extends EventMatcher> eventMatcherClass, Object event);
}
