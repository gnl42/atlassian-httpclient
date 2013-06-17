package com.atlassian.webhooks.plugin.test;

import com.atlassian.webhooks.plugin.management.WebHookListenerTransformerImpl;
import com.atlassian.webhooks.spi.provider.*;
import com.google.common.collect.ImmutableMap;

public final class TestWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar registrar)
    {
        registrar.webhook("test_event")
                .whenFired(TestEvent.class)
                .matchedBy(EventMatcher.ALWAYS_TRUE)
                .serializedWith(new EventSerializerFactory()
                {
                    @Override
                    public EventSerializer create(final Object event)
                    {
                        return new EventSerializer()
                        {
                            @Override
                            public Object getEvent()
                            {
                                return event;
                            }

                            @Override
                            public String getJson() throws EventSerializationException
                            {
                                return "{ \"value\": \"" + ((TestEvent) event).value + "\"}";
                            }
                        };
                    }
                });
        registrar.webhook("persistent_webhook")
                .whenFired(EventWithPersistentListener.class)
                .matchedBy(new EventMatcher<EventWithPersistentListener>()
                {

                    @Override
                    public boolean matches(EventWithPersistentListener event, Object listenerParameters)
                    {
                        return listenerParameters instanceof WebHookListenerTransformerImpl.RefAppListenerParameters &&
                                ((WebHookListenerTransformerImpl.RefAppListenerParameters) listenerParameters).getQualificator().equals(event.getQualificator()) &&
                                ((WebHookListenerTransformerImpl.RefAppListenerParameters) listenerParameters).getSecondaryKey().equals(event.getSecondaryKey());


                    }
                })
                .serializedWith(new EventSerializerFactory<EventWithPersistentListener>()
                {
                    @Override
                    public EventSerializer create(final EventWithPersistentListener event)
                    {
                        return EventSerializers.forMap(event, ImmutableMap.<String, Object>of("value", event.getQualificator() + event.getSecondaryKey()));
                    }
                });
    }
}
