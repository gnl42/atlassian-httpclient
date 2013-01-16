package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Collection;

public final class ModuleDescriptorWebHookRegistryImpl implements ModuleDescriptorWebHookConsumerRegistry, WebHookConsumerRegistry
{
    private final Multimap<String, WebHookConsumer> consumers = newMultimap();

    @Override
    public void register(String pluginKey, String webHookId, String consumerKey, URI uri)
    {
        consumers.put(webHookId, new WebHookConsumerImpl(pluginKey, consumerKey, uri));
    }

    @Override
    public void unregister(String pluginKey, String webHookId, String consumerKey, URI uri)
    {
        consumers.get(webHookId).remove(new WebHookConsumerImpl(pluginKey, consumerKey, uri));
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
        private final String pluginKey;
        private final String consumerKey;
        private final URI uri;

        public WebHookConsumerImpl(String pluginKey, String consumerKey, URI uri)
        {
            this.pluginKey = pluginKey;
            this.consumerKey = consumerKey;
            this.uri = uri;
        }

        @Override
        public String getPluginKey()
        {
            return pluginKey;
        }

        @Override
        public String getConsumerKey()
        {
            return consumerKey;
        }

        @Override
        public URI getPath()
        {
            return uri;
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
