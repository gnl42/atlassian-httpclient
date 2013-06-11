package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebHookConsumerCacheImpl implements WebHookConsumerCache
{
    // TODO change WebHookAO to SoftReference<WebHookAO>
    private final Map<Integer, WebHookAO> cache = new ConcurrentHashMap<Integer, WebHookAO>();

    @Override
    public void clear()
    {
        cache.clear();
    }

    @Override
    public void put(final WebHookAO webHookConsumer)
    {
        cache.put(webHookConsumer.getID(), webHookConsumer);
    }

    @Override
    public WebHookAO remove(final Integer webHookConsumerId)
    {
        return cache.remove(webHookConsumerId);
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
        return cache.values();
    }

    @Override
    public Optional<WebHookAO> get(final Integer id)
    {
        return Optional.fromNullable(cache.get(id));
    }
}
