package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookConsumerManager;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class WebHookConsumerCacheImpl implements WebHookConsumerCache
{
    private final Map<Integer, SoftReference<WebHookAO>> cache = new ConcurrentHashMap<Integer, SoftReference<WebHookAO>>();

    private final WebHookConsumerManager webHookConsumerManager;

    public WebHookConsumerCacheImpl(WebHookConsumerManager webHookConsumerManager)
    {
        this.webHookConsumerManager = webHookConsumerManager;
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public void put(final WebHookAO webHookConsumer)
    {
        cache.put(webHookConsumer.getID(), new SoftReference<WebHookAO>(webHookConsumer));
    }

    @Override
    public Optional<WebHookAO> remove(final Integer webHookConsumerId)
    {
        Optional<WebHookAO> webHookAOOptional = get(webHookConsumerId);
        cache.remove(webHookConsumerId);
        return webHookAOOptional;
    }

    @Override
    public void putAll(final Iterable<WebHookAO> webHookConsumers)
    {
        for (WebHookAO webhookDao : webHookConsumers)
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
        if (webHookAOSoftReference.get() != null)
        {
            return Optional.of(webHookAOSoftReference.get());
        }
        else
        {
            Optional<WebHookAO> webHook = webHookConsumerManager.getWebHook(id);
            if (webHook.get() != null)
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
