package com.atlassian.webhooks.plugin.provider;

import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.spi.provider.*;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

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
                .matchedBy(new EventMatcher<PluginEnabledEvent>()
                {
                    @Override
                    public boolean matches(final PluginEnabledEvent event, final Object consumerParams)
                    {
                        return consumerParams instanceof PluginModuleConsumerParams
                                && (event.getPlugin().getKey()).equals(((PluginModuleConsumerParams) consumerParams).getPluginKey());
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
