package com.atlassian.webhooks.spi.provider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EventMatchers
{

    public static <T> EventMatcher<T> and(final Iterable<EventMatcher<T>> matchers)
    {
        return new EventMatcher<T>()
        {
            @Override
            public boolean matches(final T event, final Object consumerParams)
            {
                return Iterables.all(matchers, new Predicate<EventMatcher<T>>()
                {
                    @Override
                    public boolean apply(final EventMatcher<T> matcher)
                    {
                        return matcher.matches(event, consumerParams);
                    }
                });
            }
        };
    }

    public static EventMatcher<Object> or(final Iterable<EventMatcher<Object>> matchers)
    {
        return new EventMatcher<Object>()
        {
            @Override
            public boolean matches(final Object event, final Object consumerParams)
            {
                return Iterables.any(matchers, new Predicate<EventMatcher<Object>>()
                {
                    @Override
                    public boolean apply(final EventMatcher<Object> matcher)
                    {
                        return matcher.matches(event, consumerParams);
                    }
                });
            }
        };
    }

}
