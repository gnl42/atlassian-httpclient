package com.atlassian.webhooks.plugin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.PluginEventManager;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class WebHookEventsProcessorTest
{
    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private PluginEventManager pluginEventManager;

    @Mock
    private WebHookPublisher webHookPublisher;

    @Mock
    private WebHookRegistry webHookRegistry1;

    @Mock
    private WebHookRegistry webHookRegistry2;

    private WebHookEventsProcessor processor;

    @Before
    public void setUp()
    {
        processor = new WebHookEventsProcessor(eventPublisher, pluginEventManager, webHookPublisher,
                ImmutableList.of(webHookRegistry1, webHookRegistry2));
    }

    @Test
    public void testEventIsProcessedWithNoMatchingWebHook()
    {
        final Object event = new Object();

        when(webHookRegistry1.getWebHooks(anyObject())).thenReturn(ImmutableList.<WebHookEvent>of());
        when(webHookRegistry2.getWebHooks(anyObject())).thenReturn(ImmutableList.<WebHookEvent>of());

        processor.onEvent(event);

        verify(webHookRegistry1).getWebHooks(event);
        verify(webHookRegistry2).getWebHooks(event);
        verifyZeroInteractions(webHookPublisher);
    }

    @Test
    public void testEventIsProcessedWithOneMatchingWebHook()
    {
        final Object event = new Object();
        final WebHookEvent webHookEvent = mock(WebHookEvent.class);

        when(webHookRegistry1.getWebHooks(anyObject())).thenReturn(ImmutableList.<WebHookEvent>of());
        when(webHookRegistry2.getWebHooks(anyObject())).thenReturn(ImmutableList.<WebHookEvent>of(webHookEvent));

        processor.onEvent(event);

        verify(webHookRegistry1).getWebHooks(event);
        verify(webHookRegistry2).getWebHooks(event);
        verify(webHookPublisher).publish(webHookEvent);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception
    {
        processor.afterPropertiesSet();
        verify(eventPublisher).register(processor);
    }

    @Test
    public void testDestroy() throws Exception
    {
        processor.destroy();
        verify(eventPublisher).unregister(processor);
    }
}
