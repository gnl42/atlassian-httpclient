package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookConsumerManager;
import com.google.common.base.Optional;

public interface WebHookConsumerService
{

    WebHookAO addWebHook(String name, String targetUrl, String events, String parameters, WebHookConsumerManager.WebHookRegistrationMethod registrationMethod);

    WebHookAO updateWebHook(int id, String name, String targetUrl, String events, String parameters, boolean enabled);

    void removeWebHook(int id) throws IllegalArgumentException;

    Optional<WebHookAO> getWebHook(int id);

    Optional<WebHookAO> find(int id, String url, String events, String parameters);

    Optional<WebHookAO> enableWebHook(int id, boolean flag);

    Iterable<WebHookAO> getAllWebHooks();
}
