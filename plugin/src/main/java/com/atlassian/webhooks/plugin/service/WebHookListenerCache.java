package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.google.common.base.Optional;

public interface WebHookListenerCache
{
    void clear();

    void put(WebHookAO webHookListener);

    Optional<WebHookAO> remove(Integer webHookListenerId);

    void putAll(Iterable<WebHookAO> webHookListeners);

    Iterable<WebHookAO> getAll();

    Optional<WebHookAO> get(Integer id);
}
