package com.atlassian.webhooks.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class DelegatingWebHookRegistryTest
{
    private DelegatingWebHookRegistry registry;

    @Mock
    private WebHookRegistry registry1;

    @Mock
    private WebHookRegistry registry2;

    @Before
    public void setUp()
    {
        registry = new DelegatingWebHookRegistry(ImmutableList.of(registry1, registry2));
    }

    @Test
    public void testGetWebHookIds()
    {
        final String id1 = "web_hook_id_1";
        final String id2 = "web_hook_id_2";
        final String id3 = "web_hook_id_3";

        when(registry1.getWebHookIds()).thenReturn(ImmutableList.of(id1));
        when(registry2.getWebHookIds()).thenReturn(ImmutableList.of(id2, id3));

        final Iterable<String> webHookIds = registry.getWebHookIds();
        assertEquals(3, Iterables.size(webHookIds));
        assertTrue(Iterables.contains(webHookIds, id1));
        assertTrue(Iterables.contains(webHookIds, id2));
        assertTrue(Iterables.contains(webHookIds, id3));
    }

    @Test
    public void testGetWebHooks()
    {
        final WebHookEvent webHook1 = mock(WebHookEvent.class);
        final WebHookEvent webHook2 = mock(WebHookEvent.class);
        final WebHookEvent webHook3 = mock(WebHookEvent.class);

        when(registry1.getWebHooks(any())).thenReturn(ImmutableList.of(webHook1));
        when(registry2.getWebHooks(any())).thenReturn(ImmutableList.of(webHook2, webHook3));

        final Iterable<WebHookEvent> webHooks = registry.getWebHooks(new Object());
        assertEquals(3, Iterables.size(webHooks));
        assertTrue(Iterables.contains(webHooks, webHook1));
        assertTrue(Iterables.contains(webHooks, webHook2));
        assertTrue(Iterables.contains(webHooks, webHook3));
    }
}
