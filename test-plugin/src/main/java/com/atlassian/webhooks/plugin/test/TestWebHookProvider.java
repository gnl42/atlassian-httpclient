package com.atlassian.webhooks.plugin.test;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializationException;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

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
    }
}
