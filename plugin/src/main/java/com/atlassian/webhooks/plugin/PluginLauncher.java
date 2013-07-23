package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.webhooks.plugin.store.WebHookListenerCachingStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This handles all of the "startup" stuff for the webhooks plugin.
 */
public class PluginLauncher implements InitializingBean, DisposableBean
{
    private final PluginEventManager pluginEventManager;
    private final EventPublisher eventPublisher;
    private final WebHookListenerCachingStore webHookListenerCachingStore;

    public PluginLauncher(PluginEventManager pluginEventManager,
            EventPublisher eventPublisher,
            WebHookListenerCachingStore webHookListenerCachingStore)
    {
        this.pluginEventManager = pluginEventManager;
        this.eventPublisher = eventPublisher;
        this.webHookListenerCachingStore = webHookListenerCachingStore;
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(webHookListenerCachingStore);
        eventPublisher.register(webHookListenerCachingStore);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(webHookListenerCachingStore);
        eventPublisher.unregister(webHookListenerCachingStore);
    }
}
