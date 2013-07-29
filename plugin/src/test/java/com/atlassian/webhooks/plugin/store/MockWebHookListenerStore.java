package com.atlassian.webhooks.plugin.store;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.store.WebHookListenerStore;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MockWebHookListenerStore implements WebHookListenerStore
{
    private final UserManager userManager;
    private Map<Integer, WebHookListenerParameters> store = new HashMap<Integer, WebHookListenerParameters>();
    private int nextId = 0;

    public MockWebHookListenerStore(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public WebHookListenerParameters addWebHook(final String name, final String targetUrl, final Iterable<String> events, final Map<String, Object> params, final String registrationMethod)
    {
        final WebHookListenerParameters.WebHookListenerParametersImpl webHookListenerParameters =
                new WebHookListenerParameters.WebHookListenerParametersImpl(nextId++, true, new Date(), userManager.getRemoteUsername(), name, targetUrl, params, events, registrationMethod);
        store.put(webHookListenerParameters.getId(), webHookListenerParameters);
        return store.get(webHookListenerParameters.getId());
    }

    @Override
    public WebHookListenerParameters updateWebHook(final int id, final String name, final String targetUrl, final Iterable<String> events, final Map<String, Object> params, final boolean enabled)
            throws IllegalArgumentException
    {
        final WebHookListenerParameters webHookListenerParameters = store.get(id);
        if (webHookListenerParameters == null)
        {
            throw new IllegalArgumentException();
        }
        store.put(id, new WebHookListenerParameters.WebHookListenerParametersImpl(id, enabled, new Date(), userManager.getRemoteUsername(), name, targetUrl, params, events, webHookListenerParameters.getRegistrationMethod()));
        return store.get(webHookListenerParameters.getId());
    }

    @Override
    public Optional<WebHookListenerParameters> getWebHook(final int id)
    {
        return Optional.fromNullable(store.get(id));
    }

    @Override
    public void removeWebHook(int id) throws IllegalArgumentException
    {
        store.remove(id);
    }

    @Override
    public Collection<WebHookListenerParameters> getAllWebHooks()
    {
        return store.values();
    }

    @Override
    public Optional<WebHookListenerParameters> enableWebHook(final int id, final boolean enabled)
    {
        final WebHookListenerParameters webHookListenerParameters = store.get(id);
        if (webHookListenerParameters == null)
        {
            throw new IllegalArgumentException();
        }
        store.put(id, new WebHookListenerParameters.WebHookListenerParametersImpl(
            id, enabled, new Date(), userManager.getRemoteUsername(),
            webHookListenerParameters.getName(),
            webHookListenerParameters.getUrl(),
            webHookListenerParameters.getParameters(),
            webHookListenerParameters.getEvents(),
            webHookListenerParameters.getRegistrationMethod())
        );
        return Optional.of(store.get(webHookListenerParameters.getId()));
    }
}
