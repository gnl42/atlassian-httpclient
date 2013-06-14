package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.*;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class AnnotationWebHookRegistryTest
{
    public static final String WEB_HOOK_ID = "web_hook_id";
    @Mock
    private ConstructionStrategy constructionStrategy;

    @Mock
    private EventMatcher eventMatcher;

    @Mock
    private EventSerializerFactory eventSerializerFactory;

    private WebHookRegistry registry;

    @Before
    public void setUp()
    {
        registry = new AnnotationWebHookRegistry(constructionStrategy);

        when(constructionStrategy.get(EventMatcher.class)).thenReturn(eventMatcher);
        when(constructionStrategy.get(EventSerializerFactory.class)).thenReturn(eventSerializerFactory);
    }

    @Test
    public void testGetWebHooksForValidAnnotatedEvent()
    {
        final Iterable<WebHookEvent> webHooks = registry.getWebHooks(new ValidAnnotatedEvent());
        assertEquals(1, Iterables.size(webHooks));

        final Iterable<String> webHookIds = registry.getWebHookIds();
        assertEquals(1, Iterables.size(webHookIds));
        assertTrue(Iterables.contains(webHookIds, WEB_HOOK_ID));
    }

    @Test
    public void testWebHookSerializesOnce()
    {
        ValidAnnotatedEvent event = new ValidAnnotatedEvent();
        EventSerializer eventSerializer = mock(EventSerializer.class);
        when(eventSerializer.getJson()).thenReturn("foo");
        when(eventSerializerFactory.create(event)).thenReturn(eventSerializer);
        final Iterable<WebHookEvent> webHooks = registry.getWebHooks(event);
        assertEquals(1, Iterables.size(webHooks));
        WebHookEvent webHookEvent = webHooks.iterator().next();
        webHookEvent.getJson();
        webHookEvent.getJson();
        verify(eventSerializer, times(1)).getJson();
    }

    @Test
    public void testGetWebHooksForNotAnnotatedEvent()
    {
        Iterable<WebHookEvent> webHooks = registry.getWebHooks(new Object());
        assertTrue(Iterables.isEmpty(webHooks));
        assertTrue(Iterables.isEmpty(registry.getWebHookIds()));
    }

    @Test
    public void testGetWebHooksForInvalidAnnotatedEvent()
    {
        Iterable<WebHookEvent> webHooks = registry.getWebHooks(new InvalidAnnotatedEvent());
        assertTrue(Iterables.isEmpty(webHooks));
        assertTrue(Iterables.isEmpty(registry.getWebHookIds()));
    }

    @WebHook(id = WEB_HOOK_ID, matcher = EventMatcher.class, serializerFactory = EventSerializerFactory.class)
    private static final class ValidAnnotatedEvent
    {
    }

    @WebHook(id = "", matcher = EventMatcher.class, serializerFactory = EventSerializerFactory.class)
    private static final class InvalidAnnotatedEvent
    {
    }
}
