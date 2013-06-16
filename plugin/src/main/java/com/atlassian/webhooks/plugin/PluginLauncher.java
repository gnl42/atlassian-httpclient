package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.webhooks.plugin.service.WebHookConsumerService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This handles all of the "startup" stuff for the webhooks plugin.
 */
public class PluginLauncher implements InitializingBean, DisposableBean
{
    private final PluginEventManager pluginEventManager;
    private final WebHookConsumerService webHookConsumerService;
    private final EventPublisher eventPublisher;

    public PluginLauncher(PluginEventManager pluginEventManager, WebHookConsumerService webHookConsumerService, EventPublisher eventPublisher)
    {
        this.pluginEventManager = pluginEventManager;
        this.webHookConsumerService = webHookConsumerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(webHookConsumerService);
        eventPublisher.unregister(webHookConsumerService);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(webHookConsumerService);
        eventPublisher.register(webHookConsumerService);
    }
}
