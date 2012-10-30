package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.*;

public final class WebHookEventsProcessor implements InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final WebHookPublisher webHookPublisher;
    private final WebHookRegistry registry;
    private final PluginEventManager pluginEventManager;

    public WebHookEventsProcessor(EventPublisher eventPublisher, PluginEventManager pluginEventManager, WebHookPublisher webHookPublisher, WebHookRegistry registry)
    {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.pluginEventManager = checkNotNull(pluginEventManager);
        this.webHookPublisher = checkNotNull(webHookPublisher);
        this.registry = checkNotNull(registry);
    }

    @EventListener
    public void onEvent(final Object event)
    {
        doOnEvent(event);
    }

    private void doOnEvent(Object event)
    {
        for (WebHookEvent webHookEvent : getWebHooksForEvent(event))
        {
            webHookPublisher.publish(webHookEvent);
        }
    }

    private Iterable<WebHookEvent> getWebHooksForEvent(final Object event)
    {
        return registry.getWebHooks(event);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
        pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
        eventPublisher.unregister(this);
    }
}
