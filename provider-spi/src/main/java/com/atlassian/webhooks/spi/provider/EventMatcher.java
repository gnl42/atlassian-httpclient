package com.atlassian.webhooks.spi.provider;

/**
 * Matches an event for publication with parameters of a WebHook listener.
 */
public interface EventMatcher<T>
{

    static final EventMatcher<Object> ALWAYS_TRUE = new AlwaysTrueEventMatcher();

    /**
     * Useful for registering events but then firing them manually outside the event system
     */
    static final EventMatcher<Object> ALWAYS_FALSE = new AlwaysFalseEventMatcher();

    /**
     * Tells whether the WebHook listener wants to accept a WebHook for the fired event.
     *
     * @param event the event being fired, associated to the WebHook.
     * @param listenerParameters the params of the listener waiting for the WebHook.
     * @return {@code true} if this event matches the web hook registration, {@code false} otherwise.
     */
    boolean matches(T event, Object listenerParameters);


    static final class AlwaysTrueEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(final Object event, final Object listenerParameters)
        {
            return true;
        }
    }

    static final class AlwaysFalseEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(final Object event, final Object listenerParameters)
        {
            return false;
        }
    }
}
