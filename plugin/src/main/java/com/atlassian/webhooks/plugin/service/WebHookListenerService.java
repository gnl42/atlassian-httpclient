package com.atlassian.webhooks.plugin.service;

import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.google.common.base.Optional;

public interface WebHookListenerService
{

    WebHookAO addWebHook(String name, String targetUrl, Iterable<String> events, String parameters, WebHookListenerManager.WebHookListenerRegistrationMethod registrationMethod);

    WebHookAO updateWebHook(int id, String name, String targetUrl, Iterable<String> events, String parameters, boolean enabled);

    void removeWebHook(int id) throws IllegalArgumentException;

    Optional<WebHookAO> getWebHook(int id);

    Optional<WebHookAO> find(Integer id, String url, Iterable<String> events, String parameters);

    Optional<WebHookAO> enableWebHook(int id, boolean flag);

    Iterable<WebHookAO> getAllWebHooks();
}
