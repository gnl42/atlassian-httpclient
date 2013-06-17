package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.webhooks.plugin.service.WebHookListenerService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This handles all of the "startup" stuff for the webhooks plugin.
 */
public class PluginLauncher implements InitializingBean, DisposableBean
{
    private final PluginEventManager pluginEventManager;
    private final WebHookListenerService webHookListenerService;
    private final EventPublisher eventPublisher;

    public PluginLauncher(PluginEventManager pluginEventManager, WebHookListenerService webHookListenerService, EventPublisher eventPublisher)
    {
        this.pluginEventManager = pluginEventManager;
        this.webHookListenerService = webHookListenerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(webHookListenerService);
        eventPublisher.unregister(webHookListenerService);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(webHookListenerService);
        eventPublisher.register(webHookListenerService);
    }
}
