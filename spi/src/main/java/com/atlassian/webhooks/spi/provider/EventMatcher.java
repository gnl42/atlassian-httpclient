package com.atlassian.webhooks.spi.provider;

/**
 * Matches an event for publication to web hook listeners
 */
public interface EventMatcher<T>
{
    static final EventMatcher<Object> ALWAYS_TRUE = new EventMatcher<Object>()
    {
        @Override
        public boolean matches(Object event, String pluginKey)
        {
            return true;
        }
    };

    /**
     * Useful for registering events but then firing them manually outside the event system
     */
    static final EventMatcher<Object> ALWAYS_FALSE = new EventMatcher<Object>()
    {
        @Override
        public boolean matches(Object event, String pluginKey)
        {
            return false;
        }
    };

    /**
     * Tells whether the fired event matches the web hook registration.
     *
     * @param event the event being fired, associated to the web hook
     * @param pluginKey the key of the plugin which listens to the web hook.
     * @return {@code true} if this event matches the web hook registration, {@code false} otherwise.
     */
    boolean matches(T event, String pluginKey);
}
