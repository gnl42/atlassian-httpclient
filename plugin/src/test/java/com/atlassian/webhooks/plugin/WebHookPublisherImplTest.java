package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.plugin.event.WebHookPublishQueueFullEvent;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class WebHookPublisherImplTest
{
    private WebHookPublisher publisher;

    @Mock
    private WebHookConsumerRegistry consumerRegistry;

    @Mock
    private PublishTaskFactory publishTaskFactory;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Executor executor;

    @Before
    public void setUp()
    {
        publisher = new WebHookPublisherImpl(consumerRegistry, publishTaskFactory, eventPublisher, executor);
    }

    @Test
    public void testPublishWithNoConsumers() throws Exception
    {
        when(consumerRegistry.getConsumers(Matchers.<WebHookEvent>any())).thenReturn(ImmutableList.<WebHookConsumer>of());

        publisher.publish(mock(WebHookEvent.class));

        verifyZeroInteractions(publishTaskFactory, eventPublisher, executor);
    }

    @Test
    public void testPublishWithConsumers() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        final WebHookConsumer consumer = mock(WebHookConsumer.class);
        final PublishTask publishTask = mock(PublishTask.class);

        when(consumerRegistry.getConsumers(event)).thenReturn(ImmutableList.<WebHookConsumer>of(consumer));
        when(publishTaskFactory.getPublishTask(event, consumer)).thenReturn(publishTask);
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_TRUE);

        publisher.publish(event);

        verify(publishTaskFactory).getPublishTask(event, consumer);
        verify(executor).execute(publishTask);
        verifyZeroInteractions(eventPublisher);
    }

    @Test
    public void testPublishWithFullQueue() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        when(event.getId()).thenReturn("webhook_id");
        final WebHookConsumer consumer = mock(WebHookConsumer.class);
        final PublishTask publishTask = mock(PublishTask.class);

        when(consumerRegistry.getConsumers(event)).thenReturn(ImmutableList.<WebHookConsumer>of(consumer));
        when(publishTaskFactory.getPublishTask(event, consumer)).thenReturn(publishTask);
        doThrow(RejectedExecutionException.class).when(executor).execute(publishTask);
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_TRUE);

        publisher.publish(event);

        verify(publishTaskFactory).getPublishTask(event, consumer);
        verify(executor).execute(publishTask);
        verify(eventPublisher).publish(isA(WebHookPublishQueueFullEvent.class));
    }

    @Test
    public void testPublishWithEventNotMatching() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        when(event.getId()).thenReturn("webhook_id");
        final WebHookConsumer consumer = mock(WebHookConsumer.class);

        when(consumerRegistry.getConsumers(event)).thenReturn(ImmutableList.<WebHookConsumer>of(consumer));
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_FALSE);

        publisher.publish(event);

        verifyZeroInteractions(publishTaskFactory, executor, eventPublisher);
    }
}
