package com.atlassian.webhooks.spi.provider;

/**
 * Matches an event for publication to web hook listeners
 */
public interface EventMatcher<T>
{
    static final EventMatcher<Object> ALWAYS_TRUE = new AlwaysTrueEventMatcher();

    /**
     * Useful for registering events but then firing them manually outside the event system
     */
    static final EventMatcher<Object> ALWAYS_FALSE = new AlwaysFalseEventMatcher();

    /**
     * Tells whether the fired event matches the web hook registration.
     *
     * @param event the event being fired, associated to the web hook
     * @param webHookConsumer the plugin key and consumer key of the consumer which listens to the web hook.
     * @return {@code true} if this event matches the web hook registration, {@code false} otherwise.
     */
    boolean matches(T event, ConsumerKey webHookConsumer);

    static final class AlwaysTrueEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(final Object event, final ConsumerKey webHookConsumer)
        {
            return true;
        }
    }

    static final class AlwaysFalseEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(final Object event, final ConsumerKey webHookConsumer)
        {
            return false;
        }
    }
}
