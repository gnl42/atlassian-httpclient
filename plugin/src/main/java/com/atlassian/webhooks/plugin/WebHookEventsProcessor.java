package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterables.*;

public final class WebHookEventsProcessor implements InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final WebHookPublisher webHookPublisher;
    private final Iterable<WebHookRegistry> registries;
    private final PluginEventManager pluginEventManager;

    public WebHookEventsProcessor(EventPublisher eventPublisher, PluginEventManager pluginEventManager, WebHookPublisher webHookPublisher, Iterable<WebHookRegistry> registries)
    {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.pluginEventManager = checkNotNull(pluginEventManager);
        this.webHookPublisher = checkNotNull(webHookPublisher);
        this.registries = ImmutableList.copyOf(checkNotNull(registries));
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
        return concat(transform(registries, new Function<WebHookRegistry, Iterable<WebHookEvent>>()
        {
            @Override
            public Iterable<WebHookEvent> apply(WebHookRegistry registry)
            {
                return registry.getWebHooks(event);
            }
        }));
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
