package com.atlassian.webhooks.plugin.service;

import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.webhooks.plugin.PluginProperties;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.event.WebHookEventDispatcher;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.atlassian.webhooks.spi.provider.cache.ClearWebHookListenerCacheEvent;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookListenerServiceImpl implements WebHookListenerService
{
    private final WebHookListenerCache webHookListenerCache;
    private final WebHookListenerManager webHookListenerManager;
    private final WebHookEventDispatcher webHookEventDispatcher;
    private WebHookRetrievalStrategy webHookRetrievalStrategy;

    public WebHookListenerServiceImpl(WebHookListenerCache webHookListenerCache, WebHookListenerManager webHookListenerManager, WebHookEventDispatcher webHookEventDispatcher)
    {
        this.webHookListenerCache = webHookListenerCache;
        this.webHookListenerManager = webHookListenerManager;
        this.webHookEventDispatcher = webHookEventDispatcher;
        this.webHookRetrievalStrategy = NO_OP_WEB_HOOK_RETRIEVAL_STRATEGY;
    }

    @Override
    public WebHookAO addWebHook(String name, String targetUrl, Iterable<String> events, String parameters, WebHookListenerManager.WebHookListenerRegistrationMethod registrationMethod)
    {
        checkNotNull(name);
        checkNotNull(targetUrl);
        checkNotNull(events);

        WebHookAO webHookAO = webHookListenerManager.addWebHook(name, targetUrl, WebHookListenerEventJoiner.join(events), parameters, registrationMethod);
        webHookListenerCache.put(webHookAO);
        webHookEventDispatcher.webHookCreated(webHookAO);
        return webHookAO;
    }

    @Override
    public WebHookAO updateWebHook(int id, String name, String targetUrl, Iterable<String> events, String parameters, boolean enabled)
    {
        checkNotNull(name);
        checkNotNull(targetUrl);
        checkNotNull(events);

        WebHookAO webHookAO = webHookListenerManager.updateWebHook(id, name, targetUrl, WebHookListenerEventJoiner.join(events), parameters, enabled);
        webHookListenerCache.put(webHookAO);
        webHookEventDispatcher.webHookEdited(webHookAO);
        return webHookAO;
    }

    @Override
    public void removeWebHook(int id) throws IllegalArgumentException
    {
        Optional<WebHookAO> removedWebHook = webHookListenerCache.remove(id);
        webHookEventDispatcher.webHookDeleted(removedWebHook.get());
        webHookListenerManager.removeWebHook(id);
    }

    @Override
    public Optional<WebHookAO> getWebHook(int id)
    {
        return webHookRetrievalStrategy.get(id);
    }

    @Override
    public Optional<WebHookAO> find(final Integer id, final String url, final Iterable<String> events, final String parameters)
    {
        final WebHookAO webhookDao = Iterables.find(webHookRetrievalStrategy.getAll(), new Predicate<WebHookAO>()
        {
            @Override
            public boolean apply(final WebHookAO webHookAO)
            {
                // You can't be a duplicate of yourself.
                if (id != null && id != webHookAO.getID())
                {
                    return false;
                }

                if (!StringUtils.equals(url, webHookAO.getUrl()))
                {
                    return false;
                }
                if (!StringUtils.equals(parameters, webHookAO.getParameters()))
                {
                    return false;
                }
                return StringUtils.equals(WebHookListenerEventJoiner.join(events), webHookAO.getEvents());
            }
        }, null);
        return Optional.fromNullable(webhookDao);
    }

    @Override
    public Optional<WebHookAO> enableWebHook(int id, boolean flag)
    {
        Optional<WebHookAO> webHook = webHookListenerManager.enableWebHook(id, flag);
        if (webHook.isPresent())
        {
            webHookListenerCache.put(webHook.get());

            if (flag)
            {
                webHookEventDispatcher.webHookEnabled(webHook.get());
            } else
            {
                webHookEventDispatcher.webHookDisabled(webHook.get());
            }
        }

        return webHook;
    }

    @Override
    public Iterable<WebHookAO> getAllWebHooks()
    {
        return webHookRetrievalStrategy.getAll();
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public final void onPluginStarted(final PluginEnabledEvent pluginEnabledEvent)
    {
        if (PluginProperties.PLUGIN_KEY.equals(pluginEnabledEvent.getPlugin().getKey()))
        {
            webHookRetrievalStrategy = ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY;
        }
    }

    @EventListener
    @SuppressWarnings("unused")
    public final void onClearCacheEvent(final ClearWebHookListenerCacheEvent clearCacheEvent)
    {
        webHookListenerCache.clear();
        webHookRetrievalStrategy = ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY;
    }

    private interface WebHookRetrievalStrategy
    {
        Iterable<WebHookAO> getAll();

        Optional<WebHookAO> get(Integer id);
    }


    /**
     * This strategy retrieves all webhooks from database, caches this and switches the retrieval strategy to
     * CacheWebHookRetrievalStrategy. It is used to initially populate the cache with webhooks after PluginEnabledEvent
     * or ClearCacheEvent.
     */
    private final WebHookRetrievalStrategy ACTIVE_OBJECT_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookAO> getAll()
        {
            fillCache();
            return webHookRetrievalStrategy.getAll();
        }

        @Override
        public Optional<WebHookAO> get(final Integer id)
        {
            fillCache();
            return webHookRetrievalStrategy.get(id);
        }

        private void fillCache()
        {
            Iterable<WebHookAO> allWebhooks = webHookListenerManager.getAllWebHooks();
            webHookListenerCache.clear();
            webHookListenerCache.putAll(allWebhooks);
            webHookRetrievalStrategy = CACHE_WEB_HOOK_RETRIEVAL_STRATEGY;
        }
    };

    /**
     * This strategy retrieves webhooks from cache. Used after cache is filled with webhooks.
     */
    private final WebHookRetrievalStrategy CACHE_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookAO> getAll()
        {
            return webHookListenerCache.getAll();
        }

        @Override
        public Optional<WebHookAO> get(final Integer id)
        {
            return webHookListenerCache.get(id);
        }
    };

    /**
     * This retrieval strategy should be used before the plugin is started to avoid ActiveObjectsPluginExceptions.
     */
    private final WebHookRetrievalStrategy NO_OP_WEB_HOOK_RETRIEVAL_STRATEGY = new WebHookRetrievalStrategy()
    {
        @Override
        public Iterable<WebHookAO> getAll()
        {
            return Collections.emptyList();
        }

        @Override
        public Optional<WebHookAO> get(final Integer id)
        {
            return Optional.absent();
        }
    };
}
