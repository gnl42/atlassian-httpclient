package com.atlassian.webhooks.plugin;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.event.WebHookEventDispatcher;
import com.atlassian.webhooks.plugin.manager.WebHookConsumerManager;
import com.atlassian.webhooks.plugin.manager.WebHookConsumerManagerImpl;
import com.atlassian.webhooks.plugin.service.WebHookConsumerCacheImpl;
import com.atlassian.webhooks.plugin.service.WebHookConsumerConsumerServiceImpl;
import com.google.common.base.Optional;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ActiveObjectsJUnitRunner.class)
public class WebHookListenerServiceTest
{
    private static final String LAST_UPDATED_USER = "Bobba Fett";
    private static final String WEBHOOK_NAME = "We listen for news about capturing of Han Solo";
    private static final String TARGET_URL = "http://www.superstar-destroyer.com.empire";
    private static final String EVENTS = "rebel_captured_event";
    private static final String PARAMETERS = "rebel = 'Han Solo'";

    @SuppressWarnings ("UnusedDeclaration")
    private EntityManager entityManager;
    private WebHookConsumerConsumerServiceImpl webHookConsumerConsumerService;
    private UserManager userManager;

    @Before
    public void setUp()
    {
        this.userManager = mock(UserManager.class);
        when(userManager.getRemoteUsername()).thenReturn(LAST_UPDATED_USER);

        ActiveObjects ao = new TestActiveObjects(entityManager);
        WebHookConsumerManager webHookConsumerManager = new WebHookConsumerManagerImpl(ao, userManager, mock(I18nResolver.class));
        this.webHookConsumerConsumerService = new WebHookConsumerConsumerServiceImpl(new WebHookConsumerCacheImpl(webHookConsumerManager), webHookConsumerManager, mock(WebHookEventDispatcher.class));
        //noinspection unchecked
        ao.migrate(WebHookAO.class);

        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PluginProperties.PLUGIN_KEY);
        webHookConsumerConsumerService.onPluginStarted(new PluginEnabledEvent(plugin));
    }

    @Test
    public void testWebHookRegistration()
    {
        final WebHookAO webHookAO = webHookConsumerConsumerService.addWebHook(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookConsumerManager.WebHookRegistrationMethod.REST);

        assertNotNull(webHookAO);

        final Optional<WebHookAO> exists = webHookConsumerConsumerService.find(null, TARGET_URL, EVENTS, PARAMETERS);
        assertSame(webHookAO.getID(), exists.get().getID());
    }

    @Test
    public void testWebHookRegistrationAndUpdate()
    {
        WebHookAO webHookAO = webHookConsumerConsumerService.addWebHook(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookConsumerManager.WebHookRegistrationMethod.REST);
        assertNotNull(webHookAO);

        final Optional<WebHookAO> exists = webHookConsumerConsumerService.find(webHookAO.getID(), TARGET_URL, EVENTS, PARAMETERS);
        assertSame(webHookAO.getID(), exists.get().getID());

        webHookConsumerConsumerService.updateWebHook(webHookAO.getID(), WEBHOOK_NAME, TARGET_URL, "rebel_lost_event", PARAMETERS, true);
        assertFalse("We are not listening on rebel_lost_event. We want the rebel captured.", webHookConsumerConsumerService.find(webHookAO.getID(), TARGET_URL, EVENTS, PARAMETERS).isPresent());
    }

    @Test
    public void testFindingWebHookWithDifferentUrl()
    {
        WebHookAO webHookAO = webHookConsumerConsumerService.addWebHook(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookConsumerManager.WebHookRegistrationMethod.REST);
        assertNotNull(webHookAO);

        assertFalse("Messages about capture of Han Solo should go to SuperStar Destroyer", webHookConsumerConsumerService.find(null, "http://www.death-star.com.empire", EVENTS, PARAMETERS).isPresent());
    }

    @Test
    public void testFindingWebHookWithDifferentParams()
    {
        WebHookAO webHookAO = webHookConsumerConsumerService.addWebHook(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookConsumerManager.WebHookRegistrationMethod.REST);
        assertNotNull(webHookAO);

        assertFalse("We want Han Solo, not Leia!", webHookConsumerConsumerService.find(null, TARGET_URL, EVENTS, "rebel = 'Leia'").isPresent());
    }

    @Test
    public void testUpdateOfWebHookSetsUserName()
    {
        WebHookAO webHookAO = webHookConsumerConsumerService.addWebHook(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookConsumerManager.WebHookRegistrationMethod.REST);
        assertNotNull(webHookAO);
        when(userManager.getRemoteUsername()).thenReturn("IG-88");

        WebHookAO updatedWebHook = webHookConsumerConsumerService.updateWebHook(webHookAO.getID(), WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, true);
        assertSame("IG-88", updatedWebHook.getLastUpdatedUser());
    }

}