package com.atlassian.webhooks.plugin;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterables.*;

public final class DelegatingWebHookRegistry implements WebHookRegistry
{
    private final Iterable<WebHookRegistry> registries;

    public DelegatingWebHookRegistry(Iterable<WebHookRegistry> registries)
    {
        this.registries = checkNotNull(registries);
    }

    @Override
    public Iterable<String> getWebHookIds()
    {
        return concat(transform(registries, new Function<WebHookRegistry, Iterable<String>>()
        {
            @Override
            public Iterable<String> apply(WebHookRegistry registry)
            {
                return registry.getWebHookIds();
            }
        }));
    }

    @Override
    public Iterable<WebHookEvent> getWebHooks(final Object event)
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
}
