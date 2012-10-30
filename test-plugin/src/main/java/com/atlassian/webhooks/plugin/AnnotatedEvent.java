package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.atlassian.webhooks.spi.provider.WebHook;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

@WebHook(id = "annotated_event", serializerFactory = AnnotatedEvent.AnnotatedEventSerializerFactory.class)
public final class AnnotatedEvent
{
    public final String value;

    public AnnotatedEvent(String value)
    {
        this.value = checkNotNull(value);
    }

    public static final class AnnotatedEventSerializerFactory implements EventSerializerFactory<AnnotatedEvent>
    {
        @Override
        public EventSerializer create(AnnotatedEvent event)
        {
            return EventSerializers.forMap(event, ImmutableMap.<String, Object>of("value", event.value));
        }
    }
}
