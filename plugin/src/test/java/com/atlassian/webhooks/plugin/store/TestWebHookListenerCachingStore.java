package com.atlassian.webhooks.plugin.store;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.plugin.PluginProperties;
import com.atlassian.webhooks.spi.provider.WebHookClearCacheEvent;
import com.atlassian.webhooks.spi.provider.store.WebHookListenerStore;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.EVENTS;
import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.LAST_UPDATED_USER;
import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.PARAMETERS;
import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.TARGET_URL;
import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.WEBHOOK_NAME;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWebHookListenerCachingStore
{
    private WebHookListenerCachingStore webHookListenerCachingStore;
    private WebHookListenerStore webHookListenerStore;

    @Before
    public void setup()
    {
        UserManager userManager = mock(UserManager.class);
        when(userManager.getRemoteUsername()).thenReturn(LAST_UPDATED_USER);
        webHookListenerStore = Mockito.spy(new MockWebHookListenerStore(userManager));
        webHookListenerCachingStore = new WebHookListenerCachingStore(webHookListenerStore);
    }

    @Test
    public void testRetrievingWebHooksBeforePluginFullyStarted()
    {
        webHookListenerCachingStore.registerWebHookListener(
                WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS,WebHookListenerService.RegistrationMethod.SERVICE);
        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(0));
        verify(webHookListenerStore, times(0)).getAllWebHooks();

        final Plugin plugin = Mockito.mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PluginProperties.PLUGIN_KEY);
        webHookListenerCachingStore.onPluginStarted(new PluginEnabledEvent(plugin));

        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(1));
        verify(webHookListenerStore, times(1)).getAllWebHooks();
    }

    @Test
    public void testWebHookListenerSurvivesClearCacheEvent()
    {
        webHookListenerCachingStore.registerWebHookListener(
                WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS,WebHookListenerService.RegistrationMethod.SERVICE);
        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(0));

        webHookListenerCachingStore.onClearCacheEvent(new WebHookClearCacheEvent());

        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(1));
        verify(webHookListenerStore, times(1)).getAllWebHooks();
    }
}
