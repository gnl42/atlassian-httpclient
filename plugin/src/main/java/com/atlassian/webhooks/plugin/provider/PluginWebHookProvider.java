package com.atlassian.webhooks.plugin.provider;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

public final class PluginWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar publish)
    {
        // these gets fired manually via the {@link WebHookModuleDescriptor}
        publish.webhook("plugin_enabled").whenFired(Object.class).matchedBy(EventMatcher.ALWAYS_FALSE);
    }
}
