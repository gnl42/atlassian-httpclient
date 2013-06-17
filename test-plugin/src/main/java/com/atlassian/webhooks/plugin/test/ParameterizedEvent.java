package com.atlassian.webhooks.plugin.test;

import com.atlassian.webhooks.spi.provider.*;
import com.google.common.collect.ImmutableMap;

@WebHook(id = "parameterized_event", serializerFactory = ParameterizedEvent.ParameterizedEventSerializerFactory.class, matcher = ParameterizedEvent.ParameterizedEventMatcher.class)
public final class ParameterizedEvent
{
    private final String value;

    public ParameterizedEvent(final String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public static final class ParameterizedEventSerializerFactory implements EventSerializerFactory<ParameterizedEvent>
    {
        @Override
        public EventSerializer create(final ParameterizedEvent event)
        {
            return EventSerializers.forMap(event, ImmutableMap.<String, Object>of("value", event.value));
        }
    }

    public static final class ParameterizedEventMatcher implements EventMatcher<ParameterizedEvent>
    {

        @Override
        public boolean matches(final ParameterizedEvent event, final Object listenerParameters)
        {
            return listenerParameters instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) listenerParameters).getParams().get("webhookParam").equals(event.value);
        }
    }
}
