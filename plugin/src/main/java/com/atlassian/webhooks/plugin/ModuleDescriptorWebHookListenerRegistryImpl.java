package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listener registry for WebHookListeners declared in atlassian-plugin.xml via <webhook> descriptor.
 */
public final class ModuleDescriptorWebHookListenerRegistryImpl implements ModuleDescriptorWebHookListenerRegistry, WebHookListenerProvider
{
    private final Multimap<String, WebHookListener> listeners = newMultimap();

    @Override
    public void register(final String webHookId, final String pluginKey, final URI uri, final PluginModuleListenerParameters consumerParams)
    {
        listeners.put(webHookId, new WebHookListenerImpl(pluginKey, uri, consumerParams));
    }

    @Override
    public void unregister(final String webHookId, final String pluginKey, final URI uri, final PluginModuleListenerParameters consumerParams)
    {
        listeners.get(webHookId).remove(new WebHookListenerImpl(pluginKey, uri, consumerParams));
    }

    @Override
    public Iterable<WebHookListener> getListeners(WebHookEvent webHookEvent)
    {
        return listeners.get(webHookEvent.getId());
    }

    private static Multimap<String, WebHookListener> newMultimap()
    {
        return Multimaps.synchronizedMultimap(
                Multimaps.newMultimap(Maps.<String, Collection<WebHookListener>>newHashMap(),
                        new Supplier<Collection<WebHookListener>>()
                        {
                            public Collection<WebHookListener> get()
                            {
                                return Sets.newHashSet();
                            }
                        }));
    }

    private static final class WebHookListenerImpl implements WebHookListener
    {

        private final URI uri;
        private final String pluginKey;
        private final PluginModuleListenerParameters params;

        public WebHookListenerImpl(String pluginKey, URI uri, PluginModuleListenerParameters params)
        {
            this.pluginKey = checkNotNull(pluginKey);
            this.uri = checkNotNull(uri);
            this.params = checkNotNull(params);
        }

        @Override
        public String getPluginKey()
        {
            return pluginKey;
        }

        @Override
        public URI getPath()
        {
            return uri;
        }

        @Override
        public Object getListenerParameters()
        {
            return params;
        }

        @Override
        public String getConsumableBodyJson(final String json)
        {
            return json;
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(pluginKey, uri);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final WebHookListenerImpl other = (WebHookListenerImpl) obj;

            return Objects.equal(this.pluginKey, other.pluginKey)
                    && Objects.equal(this.uri, other.uri);
        }
    }
}
