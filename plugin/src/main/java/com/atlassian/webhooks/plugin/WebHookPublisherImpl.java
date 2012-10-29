package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.plugin.event.WebHookPublishQueueFullEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;

public final class WebHookPublisherImpl implements WebHookPublisher
{
    private static final int PUBLISH_QUEUE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebHookConsumerRegistry consumerRegistry;
    private final PublishTaskFactory publishTaskFactory;
    private final EventPublisher eventPublisher;
    private final Executor executor;

    public WebHookPublisherImpl(WebHookConsumerRegistry consumerRegistry, PublishTaskFactory publishTaskFactory, EventPublisher eventPublisher)
    {
        this(consumerRegistry, publishTaskFactory, eventPublisher, newDefaultExecutor());
    }

    public WebHookPublisherImpl(WebHookConsumerRegistry consumerRegistry, PublishTaskFactory publishTaskFactory, EventPublisher eventPublisher, Executor executor)
    {
        this.consumerRegistry = checkNotNull(consumerRegistry);
        this.publishTaskFactory = checkNotNull(publishTaskFactory);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.executor = checkNotNull(executor);
    }

    @Override
    public void publish(WebHookEvent webHookEvent)
    {
        for (WebHookConsumer consumer : consumerRegistry.getConsumers(webHookEvent))
        {
            if (match(webHookEvent, consumer))
            {
                publish(webHookEvent, consumer);
            }
        }
    }

    private boolean match(WebHookEvent webHookEvent, WebHookConsumer consumer)
    {
        return webHookEvent.getEventMatcher().matches(webHookEvent.getEvent(), consumer.getPluginKey());
    }

    private void publish(WebHookEvent webHookEvent, WebHookConsumer consumer)
    {
        final PublishTask publishTask = publishTaskFactory.getPublishTask(webHookEvent, consumer);
        try
        {
            executor.execute(publishTask);
        }
        catch (RejectedExecutionException ex)
        {
            logger.warn("Web hook queue full, rejecting '{}'", publishTask);
            eventPublisher.publish(new WebHookPublishQueueFullEvent(webHookEvent.getId()));
        }
    }

    private static Executor newDefaultExecutor()
    {
        return new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(PUBLISH_QUEUE_SIZE));
    }
}
