package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.WebHook;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.*;

public final class AnnotationWebHookRegistry implements WebHookRegistry
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<String> knownWebHookIds = new CopyOnWriteArraySet<String>();
    private final ConstructionStrategy constructionStrategy;

    public AnnotationWebHookRegistry()
    {
        this(new DefaultConstructorConstructionStrategy());
    }

    public AnnotationWebHookRegistry(ConstructionStrategy constructionStrategy)
    {
        this.constructionStrategy = checkNotNull(constructionStrategy);
    }

    @Override
    public Iterable<String> getWebHookIds()
    {
        return ImmutableSet.copyOf(knownWebHookIds);
    }

    @Override
    public Iterable<WebHookEvent> getWebHooks(Object event)
    {
        if (isValidWebHook(event))
        {
            final WebHook annotation = getWebHookAnnotation(event);
            knownWebHookIds.add(annotation.id()); // keep track of IDs
            return ImmutableList.of(getWebHook(event, annotation));
        }
        else
        {
            return ImmutableList.of();
        }
    }

    private WebHookEvent getWebHook(final Object event, final WebHook annotation)
    {
        return new WebHookEventImpl(event, annotation, constructionStrategy);
    }

    private boolean isAnnotatedWebHook(Object event)
    {
        return event.getClass().isAnnotationPresent(WebHook.class);
    }

    private WebHook getWebHookAnnotation(Object event)
    {
        return event.getClass().getAnnotation(WebHook.class);
    }

    private boolean isValidWebHook(Object event)
    {
        if (!isAnnotatedWebHook(event))
        {
            return false;
        }

        final WebHook webHookAnnotation = getWebHookAnnotation(event);
        if (Strings.isNullOrEmpty(webHookAnnotation.id()))
        {
            logger.warn("Event {} is not a valid (annotated) web hook as its ID is not defined", event);
            return false;
        }

        return true;
    }

    private static final class WebHookEventImpl implements WebHookEvent
    {
        private final String id;
        private final Object event;
        private final EventMatcher<Object> eventMatcher;
        private final Supplier<String> jsonSupplier;

        public WebHookEventImpl(
                final Object event, final WebHook annotation, final ConstructionStrategy constructionStrategy)
        {
            this.id = checkNotNull(annotation).id();
            this.event = checkNotNull(event);
            this.eventMatcher = checkNotNull(constructionStrategy).get(annotation.matcher());
            this.jsonSupplier = Suppliers.memoize(new Supplier<String>()
            {
                @Override
                public String get()
                {
                    EventSerializer eventSerializer = constructionStrategy.get(annotation.serializerFactory()).create(event);
                    return eventSerializer.getJson();
                }
            });
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public Object getEvent()
        {
            return event;
        }

        @Override
        public EventMatcher<Object> getEventMatcher()
        {
            return eventMatcher;
        }

        @Override
        public String getJson()
        {
            return jsonSupplier.get();
        }
    }
}
