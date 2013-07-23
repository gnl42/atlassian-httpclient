package com.atlassian.webhooks.plugin.store;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.plugin.PluginProperties;
import com.atlassian.webhooks.plugin.ao.WebHookListenerAO;
import com.atlassian.webhooks.spi.provider.WebHookClearCacheEvent;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static com.atlassian.webhooks.plugin.service.WebHookListenerServiceTest.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (ActiveObjectsJUnitRunner.class)
public class TestWebHookListenerCachingStore
{
    private WebHookListenerCachingStore webHookListenerCachingStore;
    @SuppressWarnings ("UnusedDeclaration")
    private EntityManager entityManager;
    private WebHookListenerStore webHookListenerStore;

    @Before
    public void setup()
    {
        ActiveObjects ao = new TestActiveObjects(entityManager);
        //noinspection unchecked
        ao.migrate(WebHookListenerAO.class);
        UserManager userManager = mock(UserManager.class);
        when(userManager.getRemoteUsername()).thenReturn(LAST_UPDATED_USER);
        webHookListenerStore = Mockito.spy(new WebHookListenerStore(ao, userManager, mock(I18nResolver.class)));
        webHookListenerCachingStore = new WebHookListenerCachingStore(webHookListenerStore);
    }

    @Test
    public void testRetrievingWebHooksBeforePluginFullyStarted()
    {
        final WebHookListenerParameters webHookListenerParameters = webHookListenerCachingStore.registerWebHookListener(
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
        final WebHookListenerParameters webHookListenerParameters = webHookListenerCachingStore.registerWebHookListener(
                WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS,WebHookListenerService.RegistrationMethod.SERVICE);
        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(0));

        webHookListenerCachingStore.onClearCacheEvent(new WebHookClearCacheEvent());

        assertThat(newArrayList(webHookListenerCachingStore.getAllWebHookListeners()), IsCollectionWithSize.hasSize(1));
        verify(webHookListenerStore, times(1)).getAllWebHooks();
    }
}
