package com.atlassian.webhooks.plugin;

import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.webhooks.plugin.service.InternalWebHookListenerService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This handles all of the "startup" stuff for the webhooks plugin.
 */
public class PluginLauncher implements InitializingBean, DisposableBean
{
    private final PluginEventManager pluginEventManager;
    private final InternalWebHookListenerService internalWebHookListenerService;

    public PluginLauncher(PluginEventManager pluginEventManager, InternalWebHookListenerService internalWebHookListenerService)
    {
        this.pluginEventManager = pluginEventManager;
        this.internalWebHookListenerService = internalWebHookListenerService;
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(internalWebHookListenerService);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        pluginEventManager.register(internalWebHookListenerService);
    }
}
