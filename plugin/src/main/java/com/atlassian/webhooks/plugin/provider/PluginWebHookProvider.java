package com.atlassian.webhooks.plugin.provider;

import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.*;

public final class PluginWebHookProvider implements WebHookProvider
{
    private final ApplicationProperties applicationProperties;

    public PluginWebHookProvider(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
    }

    @Override
    public void provide(WebHookRegistrar publish)
    {
        publish.webhook("plugin_enabled")
                .whenFired(PluginEnabledEvent.class)
                .matchedBy(new EventMatcher()
                {
                    @Override
                    public boolean matches(Object event, String pluginKey)
                    {
                        return event instanceof PluginEnabledEvent
                                && (((PluginEnabledEvent) event).getPlugin().getKey()).equals(pluginKey);
                    }
                })
                .serializedWith(new EventSerializerFactory()
                {
                    @Override
                    public EventSerializer create(Object event)
                    {
                        return EventSerializers.forMap(event, ImmutableMap.<String, Object>of(
                                "key", ((PluginEnabledEvent) event).getPlugin().getKey(),
                                "baseUrl", Strings.nullToEmpty(applicationProperties.getBaseUrl())
                        ));
                    }
                });
    }
}
