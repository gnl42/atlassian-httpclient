package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;

public interface WebHookConsumerCache
{
    void clear();

    void put(WebHookAO webHookConsumer);

    Optional<WebHookAO> remove(Integer webHookConsumerId);

    void putAll(Iterable<WebHookAO> webHookConsumers);

    Iterable<WebHookAO> getAll();

    Optional<WebHookAO> get(Integer id);
}
