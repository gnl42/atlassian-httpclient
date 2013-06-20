package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.plugin.event.WebHookPublishRejectedAnalyticsEvent;
import com.atlassian.webhooks.plugin.event.WebHookPublishedAnalyticsEvent;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookPublisher;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class WebHookPublisherImplTest
{
    private WebHookPublisher publisher;

    @Mock
    private WebHookListenerProvider listenerProvider;

    @Mock
    private PublishTaskFactory publishTaskFactory;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Executor executor;

    @Before
    public void setUp()
    {
        publisher = new WebHookPublisherImpl(listenerProvider, publishTaskFactory, eventPublisher, executor);
    }

    @Test
    public void testPublishWithNoListeners() throws Exception
    {
        when(listenerProvider.getListeners(Matchers.<WebHookEvent>any())).thenReturn(ImmutableList.<WebHookListener>of());

        publisher.publish(mock(WebHookEvent.class));

        verifyZeroInteractions(publishTaskFactory, eventPublisher, executor);
    }

    @Test
    public void testPublishWithListeners() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        final WebHookListener listener = mock(WebHookListener.class);
        when(listener.getPath()).thenReturn(new URI("/"));
        final PublishTask publishTask = mock(PublishTask.class);

        when(listenerProvider.getListeners(event)).thenReturn(ImmutableList.<WebHookListener>of(listener));
        when(publishTaskFactory.getPublishTask(event, listener)).thenReturn(publishTask);
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_TRUE);

        publisher.publish(event);

        verify(publishTaskFactory).getPublishTask(event, listener);
        verify(executor).execute(publishTask);
        verify(eventPublisher).publish(isA(WebHookPublishedAnalyticsEvent.class));
    }

    @Test
    public void testPublishWithFullQueue() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        when(event.getId()).thenReturn("webhook_id");
        final WebHookListener listener = mock(WebHookListener.class);
        when(listener.getPath()).thenReturn(new URI("/"));
        final PublishTask publishTask = mock(PublishTask.class);

        when(listenerProvider.getListeners(event)).thenReturn(ImmutableList.<WebHookListener>of(listener));
        when(publishTaskFactory.getPublishTask(event, listener)).thenReturn(publishTask);
        doThrow(RejectedExecutionException.class).when(executor).execute(publishTask);
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_TRUE);

        publisher.publish(event);

        verify(publishTaskFactory).getPublishTask(event, listener);
        verify(executor).execute(publishTask);
        verify(eventPublisher).publish(isA(WebHookPublishRejectedAnalyticsEvent.class));
    }

    @Test
    public void testPublishWithEventNotMatching() throws Exception
    {
        final WebHookEvent event = mock(WebHookEvent.class);
        when(event.getId()).thenReturn("webhook_id");
        final WebHookListener listener = mock(WebHookListener.class);

        when(listenerProvider.getListeners(event)).thenReturn(ImmutableList.<WebHookListener>of(listener));
        when(event.getEventMatcher()).thenReturn(EventMatcher.ALWAYS_FALSE);

        publisher.publish(event);

        verifyZeroInteractions(publishTaskFactory, executor, eventPublisher);
    }
}
