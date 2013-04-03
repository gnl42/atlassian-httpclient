package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.PluginModuleConsumerParams;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ModuleDescriptorWebHookRegistryImpl implements ModuleDescriptorWebHookConsumerRegistry, WebHookConsumerProvider
{
    private final Multimap<String, WebHookConsumer> consumers = newMultimap();

    @Override
    public void register(final String webHookId, final String pluginKey, final URI uri, final PluginModuleConsumerParams consumerParams)
    {
        consumers.put(webHookId, new WebHookConsumerImpl(pluginKey, uri, consumerParams));
    }

    @Override
    public void unregister(final String webHookId, final String pluginKey, final URI uri, final PluginModuleConsumerParams consumerParams)
    {
        consumers.get(webHookId).remove(new WebHookConsumerImpl(pluginKey, uri, consumerParams));
    }

    @Override
    public Iterable<WebHookConsumer> getConsumers(WebHookEvent webHookEvent)
    {
        return consumers.get(webHookEvent.getId());
    }

    private static Multimap<String, WebHookConsumer> newMultimap()
    {
        return Multimaps.synchronizedMultimap(
                Multimaps.newMultimap(Maps.<String, Collection<WebHookConsumer>>newHashMap(),
                        new Supplier<Collection<WebHookConsumer>>()
                        {
                            public Collection<WebHookConsumer> get()
                            {
                                return Sets.newHashSet();
                            }
                        }));
    }

    private static final class WebHookConsumerImpl implements WebHookConsumer
    {

        private final URI uri;
        private final String pluginKey;
        private final PluginModuleConsumerParams params;

        public WebHookConsumerImpl(String pluginKey, URI uri, PluginModuleConsumerParams params)
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
        public Object getConsumerParams()
        {
            return params;
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
            final WebHookConsumerImpl other = (WebHookConsumerImpl) obj;

            return Objects.equal(this.pluginKey, other.pluginKey)
                    && Objects.equal(this.uri, other.uri);
        }
    }
}
