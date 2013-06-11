package com.atlassian.webhooks.plugin;

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

    public PluginLauncher(PluginEventManager pluginEventManager, WebHookConsumerService webHookConsumerService)
    {
        this.pluginEventManager = pluginEventManager;
        this.webHookConsumerService = webHookConsumerService;
    }

//    /**
//     * Second event in the lifecycle: System runs (this comes after the plugin framework events)
//     */
//    @Override
//    public final void onStart()
//    {
//        eventPublisher.register(webHookConsumerService);
//    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(webHookConsumerService);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(webHookConsumerService);
    }
}
