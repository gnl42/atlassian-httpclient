package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.ConsumerKey;
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
    public void register(String webHookId, ConsumerKey consumerKey, URI uri)
    {
        consumers.put(webHookId, new WebHookConsumerImpl(consumerKey, uri));
    }

    @Override
    public void unregister(String webHookId, ConsumerKey consumerKey, URI uri)
    {
        consumers.get(webHookId).remove(new WebHookConsumerImpl(consumerKey, uri));
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
        private final ConsumerKey consumerKey;

        public WebHookConsumerImpl(ConsumerKey consumerKey, URI uri)
        {
            this.consumerKey = consumerKey;
            this.uri = uri;
        }

        @Override
        public ConsumerKey getConsumerKey()
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
            return Objects.hashCode(consumerKey, uri);
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

            return Objects.equal(this.consumerKey, other.consumerKey)
                    && Objects.equal(this.uri, other.uri);
        }
    }
}
