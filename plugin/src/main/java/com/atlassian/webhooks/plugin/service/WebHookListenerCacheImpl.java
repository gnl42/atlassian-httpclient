package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class WebHookListenerCacheImpl implements WebHookListenerCache
{
    private final Map<Integer, SoftReference<WebHookAO>> cache = new ConcurrentHashMap<Integer, SoftReference<WebHookAO>>();

    private final WebHookListenerManager webHookListenerManager;

    public WebHookListenerCacheImpl(WebHookListenerManager webHookListenerManager)
    {
        this.webHookListenerManager = webHookListenerManager;
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public void put(final WebHookAO webHookListener)
    {
        cache.put(webHookListener.getID(), new SoftReference<WebHookAO>(webHookListener));
    }

    @Override
    public Optional<WebHookAO> remove(final Integer webHookListenerId)
    {
        Optional<WebHookAO> webHookAOOptional = get(webHookListenerId);
        cache.remove(webHookListenerId);
        return webHookAOOptional;
    }

    @Override
    public void putAll(final Iterable<WebHookAO> webHookListeners)
    {
        for (WebHookAO webhookDao : webHookListeners)
        {
            put(webhookDao);
        }
    }

    @Override
    public Iterable<WebHookAO> getAll()
    {
        return filter(transform(cache.keySet(), new Function<Integer, WebHookAO>()
        {
            @Override
            public WebHookAO apply(final Integer id)
            {
                return get(id).get();
            }
        }), Predicates.notNull());
    }

    @Override
    public Optional<WebHookAO> get(final Integer id)
    {
        SoftReference<WebHookAO> webHookAOSoftReference = cache.get(id);
        if (webHookAOSoftReference != null && webHookAOSoftReference.get() != null)
        {
            return Optional.of(webHookAOSoftReference.get());
        }
        else
        {
            Optional<WebHookAO> webHook = webHookListenerManager.getWebHook(id);
            if (webHook.isPresent())
            {
                cache.put(id, new SoftReference<WebHookAO>(webHook.get()));
                return Optional.of(webHook.get());
            }
            else
            {
                return Optional.absent();
            }
        }
    }
}
