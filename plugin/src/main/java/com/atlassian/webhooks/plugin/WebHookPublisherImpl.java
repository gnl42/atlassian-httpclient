package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.plugin.event.WebHookPublishRejectedAnalyticsEvent;
import com.atlassian.webhooks.plugin.event.WebHookPublishedAnalyticsEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookPublisherImpl implements WebHookPublisher
{
    private static final int PUBLISH_QUEUE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebHookListenerProvider listenerProvider;
    private final PublishTaskFactory publishTaskFactory;
    private final EventPublisher eventPublisher;
    private final Executor executor;

    public WebHookPublisherImpl(WebHookListenerProvider listenerProvider, PublishTaskFactory publishTaskFactory, EventPublisher eventPublisher)
    {
        this(listenerProvider, publishTaskFactory, eventPublisher, newDefaultExecutor());
    }

    public WebHookPublisherImpl(WebHookListenerProvider listenerProvider, PublishTaskFactory publishTaskFactory, EventPublisher eventPublisher, Executor executor)
    {
        this.listenerProvider = checkNotNull(listenerProvider);
        this.publishTaskFactory = checkNotNull(publishTaskFactory);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.executor = checkNotNull(executor);
    }

    @Override
    public void publish(WebHookEvent webHookEvent)
    {
        for (WebHookListener listener : listenerProvider.getListeners(webHookEvent))
        {
            if (match(webHookEvent, listener))
            {
                publish(webHookEvent, listener);
            }
        }
    }

    private boolean match(WebHookEvent webHookEvent, WebHookListener listener)
    {
        return webHookEvent.getEventMatcher().matches(webHookEvent.getEvent(), listener.getListenerParameters());
    }

    private void publish(WebHookEvent webHookEvent, WebHookListener listener)
    {
        final PublishTask publishTask = publishTaskFactory.getPublishTask(webHookEvent, listener);
        try
        {
            executor.execute(publishTask);
            eventPublisher.publish(new WebHookPublishedAnalyticsEvent(webHookEvent.getId(), listener.getPluginKey(), listener.getPath().toString()));
        }
        catch (RejectedExecutionException ex)
        {
            logger.warn("Executor rejected the web hook '{}' saying '{}'", publishTask, ex.getMessage());
            logger.debug("Here is the full exception", ex);
            eventPublisher.publish(new WebHookPublishRejectedAnalyticsEvent(webHookEvent.getId(), listener.getPluginKey(), listener.getPath().toString(), ex.getMessage()));
        }
    }

    private static Executor newDefaultExecutor()
    {
        return new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(PUBLISH_QUEUE_SIZE));
    }
}
