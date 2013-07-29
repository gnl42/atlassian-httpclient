package com.atlassian.webhooks.plugin.store;

import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.plugin.PluginProperties;
import com.atlassian.webhooks.spi.provider.WebHookClearCacheEvent;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.store.WebHookListenerStore;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Stores and caches WebHookListeners.
 */
public class WebHookListenerCachingStore
{
    private final Map<Integer, WebHookListenerParameters> cache = new ConcurrentHashMap<Integer, WebHookListenerParameters>();
    private final WebHookListenerStore webHookListenerStore;
    private WebHookRetrievalStrategy webHookRetrievalStrategy;

    public WebHookListenerCachingStore(WebHookListenerStore webHookListenerStore)
    {
        this.webHookListenerStore = checkNotNull(webHookListenerStore);
        this.webHookRetrievalStrategy = NO_OP_WEB_HOOK_RETRIEVAL_STRATEGY;
    }

    /**
     * Get a single WebHook Listener by id.
     *
     * @param id of the WebHook Listener.
     * @return the WebHook listener.
     */
    public Optional<WebHookListenerParameters> getWebHookListener(final Integer id)
    {
        return webHookRetrievalStrategy.get(id);
    }

    /**
     * Get a list of all listeners in the system
     * @return collection of WebHook listeners.
     */
    public Iterable<WebHookListenerParameters> getAllWebHookListeners()
    {
        return webHookRetrievalStrategy.getAll();
    }

    /**
     * Add and caches a new WebHook listener and returns the newly created WebHook listener.
     *
     * @param name WebHook Listener name.
     * @param url where response will be sent.
     * @param events list of events.
     * @param parameters parameters of the listener.
     * @param registrationMethod REST, UI or SERVICE.
     */
    public WebHookListenerParameters registerWebHookListener(String name, String url, Iterable<String> events, Map<String, Object> parameters,
            WebHookListenerService.RegistrationMethod registrationMethod)
    {
        final WebHookListenerParameters listenerParameters = webHookListenerStore.addWebHook(name, url, events, parameters, registrationMethod.name());
        cache.put(listenerParameters.getId(), listenerParameters);
        return listenerParameters;
    }

    /**
     * Updates existing WebHook listener in db and cache and returns the newly created WebHook.
     *
     * @param id WebHook listener id.
     * @param name WebHook listener name.
     * @param url url where response will be sent.
     * @param events list of events.
     * @param parameters parameters of the WebHook Listener.
     * @param isEnabled indicates whether a WebHook Listener is enabled.
     * @throws IllegalArgumentException when listener with the specified id doesn't exist.
     */
    public WebHookListenerParameters updateWebHookListener(int id, String name, String url, Iterable<String> events,
            Map<String, Object> parameters, Boolean isEnabled) throws IllegalArgumentException
    {
        boolean enabled = isEnabled != null ? isEnabled : cache.get(id).isEnabled();
        final WebHookListenerParameters listenerParameters = webHookListenerStore.updateWebHook(id, name, url, events, parameters, enabled);
        cache.put(listenerParameters.getId(), listenerParameters);
        return listenerParameters;
    }

    /**
     * Removes single WebHook Listener by id from db and cache.
     *
     * @param id of the WebHook Listener.
     * @throws IllegalArgumentException the specified id does not exist
     */
    public void removeWebHookListener(final int id)
    {
        webHookListenerStore.removeWebHook(id);
        cache.remove(id);
    }

    /**
     * Enables/disables WebHook listener.
     * @param id id of the listener to enable.
     * @param flag true for enabling the listener, else false.
     * @return the changed listener, else none.
     * @throws IllegalArgumentException the specified id does not exist
     */
    public Optional<WebHookListenerParameters> enableWebHook(final int id, final boolean flag)
    {
        final Optional<WebHookListenerParameters> webHookListenerParameters = webHookListenerStore.enableWebHook(id, flag);
        if (webHookListenerParameters.isPresent())
        {
            cache.put(id, webHookListenerParameters.get());
        }
        return webHookListenerParameters;
    }

    @PluginEventListener @SuppressWarnings("unused")
    public final void onPluginStarted(final PluginEnabledEvent pluginEnabledEvent)
    {
        if (PluginProperties.PLUGIN_KEY.equals(pluginEnabledEvent.getPlugin().getKey()))
        {
            webHookRetrievalStrategy = ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY;
        }
    }

    @EventListener @SuppressWarnings("unused")
    public void onClearCacheEvent(final WebHookClearCacheEvent clearCacheEvent)
    {
        cache.clear();
        webHookRetrievalStrategy = ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY;
    }

    private interface WebHookRetrievalStrategy
    {
        Iterable<WebHookListenerParameters> getAll();

        Optional<WebHookListenerParameters> get(Integer id);
    }


    /**
     * This strategy retrieves all webhooks from database, caches this and switches the retrieval strategy to
     * CacheWebHookRetrievalStrategy. It is used to initially populate the cache with webhooks after PluginEnabledEvent
     * or ClearCacheEvent.
     */
    private final WebHookRetrievalStrategy ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookListenerParameters> getAll()
        {
            fillCache();
            return webHookRetrievalStrategy.getAll();
        }

        @Override
        public Optional<WebHookListenerParameters> get(final Integer id)
        {
            fillCache();
            return webHookRetrievalStrategy.get(id);
        }

        private void fillCache()
        {
            Iterable<WebHookListenerParameters> allWebhooks = webHookListenerStore.getAllWebHooks();
            cache.clear();
            for (WebHookListenerParameters listenerParams : allWebhooks)
            {
                cache.put(listenerParams.getId(), listenerParams);
            }
            webHookRetrievalStrategy = CACHE_WEB_HOOK_RETRIEVAL_STRATEGY;
        }
    };

    /**
     * This strategy retrieves webhooks from cache. Used after cache is filled with webhooks.
     */
    private final WebHookRetrievalStrategy CACHE_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookListenerParameters> getAll()
        {
            return cache.values();
        }

        @Override
        public Optional<WebHookListenerParameters> get(final Integer id)
        {
            return Optional.fromNullable(cache.get(id));
        }
    };

    /**
     * This retrieval strategy should be used before the plugin is started to avoid ActiveObjectsPluginExceptions.
     */
    private final WebHookRetrievalStrategy NO_OP_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookListenerParameters> getAll()
        {
            return Collections.emptyList();
        }

        @Override
        public Optional<WebHookListenerParameters> get(final Integer id)
        {
            return Optional.absent();
        }
    };

}
