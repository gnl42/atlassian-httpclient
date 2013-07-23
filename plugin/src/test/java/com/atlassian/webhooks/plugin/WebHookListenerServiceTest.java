package com.atlassian.webhooks.plugin;

import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Lists;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.runner.RunWith;

@RunWith(ActiveObjectsJUnitRunner.class)
public class WebHookListenerServiceTest
{
    private static final String LAST_UPDATED_USER = "Bobba Fett";
    private static final String WEBHOOK_NAME = "We listen for news about capturing of Han Solo";
    private static final String TARGET_URL = "http://www.superstar-destroyer.com.empire";
    private static final Iterable<String> EVENTS = Lists.newArrayList("rebel_captured_event");
    private static final String PARAMETERS = "rebel = 'Han Solo'";

    @SuppressWarnings ("UnusedDeclaration")
    private EntityManager entityManager;
//    private WebHookListenerServiceImpl webHookListenerService;
    private UserManager userManager;

//    @Before
//    public void setUp()
//    {
//        this.userManager = mock(UserManager.class);
//        when(userManager.getRemoteUsername()).thenReturn(LAST_UPDATED_USER);
//
//        ActiveObjects ao = new TestActiveObjects(entityManager);
//        WebHookListenerStore webHookListenerStore = new WebHookListenerStore(ao, userManager, mock(I18nResolver.class));
//        this.webHookListenerService = new WebHookListenerServiceImpl(new WebHookListenerCacheImpl(webHookListenerStore), webHookListenerStore, mock(WebHookEventDispatcher.class));
//        //noinspection unchecked
//        ao.migrate(WebHookAO.class);
//
//        Plugin plugin = mock(Plugin.class);
//        when(plugin.getKey()).thenReturn(PluginProperties.PLUGIN_KEY);
//        webHookListenerService.onPluginStarted(new PluginEnabledEvent(plugin));
//    }
//
//    @Test
//    public void testWebHookRegistration()
//    {
//        final WebHookAO webHookAO = webHookListenerService.addWebHookListener(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookListenerStore.WebHookListenerRegistrationMethod.REST);
//
//        assertNotNull(webHookAO);
//
//        final Optional<WebHookAO> exists = webHookListenerService.findWebHookListener(null, TARGET_URL, EVENTS, PARAMETERS);
//        assertSame(webHookAO.getID(), exists.get().getID());
//    }
//
//    @Test
//    public void testWebHookRegistrationAndUpdate()
//    {
//        WebHookAO webHookAO = webHookListenerService.addWebHookListener(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookListenerStore.WebHookListenerRegistrationMethod.REST);
//        assertNotNull(webHookAO);
//
//        final Optional<WebHookAO> exists = webHookListenerService.findWebHookListener(null, TARGET_URL, EVENTS, PARAMETERS);
//        assertSame(webHookAO.getID(), exists.get().getID());
//
//        webHookListenerService.updateWebHookListener(webHookAO.getID(), WEBHOOK_NAME, TARGET_URL, Lists.newArrayList("rebel_lost_event"), PARAMETERS, true);
//        assertFalse("We are not listening on rebel_lost_event. We want the rebel captured.", webHookListenerService.findWebHookListener(webHookAO.getID(), TARGET_URL, EVENTS, PARAMETERS).isPresent());
//    }
//
//    @Test
//    public void testFindingWebHookWithDifferentUrl()
//    {
//        WebHookAO webHookAO = webHookListenerService.addWebHookListener(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookListenerStore.WebHookListenerRegistrationMethod.REST);
//        assertNotNull(webHookAO);
//
//        assertFalse("Messages about capture of Han Solo should go to SuperStar Destroyer", webHookListenerService.findWebHookListener(null, "http://www.death-star.com.empire", EVENTS, PARAMETERS).isPresent());
//    }
//
//    @Test
//    public void testFindingWebHookWithDifferentParams()
//    {
//        WebHookAO webHookAO = webHookListenerService.addWebHookListener(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookListenerStore.WebHookListenerRegistrationMethod.REST);
//        assertNotNull(webHookAO);
//
//        assertFalse("We want Han Solo, not Leia!", webHookListenerService.findWebHookListener(null, TARGET_URL, EVENTS, "rebel = 'Leia'").isPresent());
//    }
//
//    @Test
//    public void testUpdateOfWebHookSetsUserName()
//    {
//        WebHookAO webHookAO = webHookListenerService.addWebHookListener(WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, WebHookListenerStore.WebHookListenerRegistrationMethod.REST);
//        assertNotNull(webHookAO);
//        when(userManager.getRemoteUsername()).thenReturn("IG-88");
//
//        WebHookAO updatedWebHook = webHookListenerService.updateWebHookListener(webHookAO.getID(), WEBHOOK_NAME, TARGET_URL, EVENTS, PARAMETERS, true);
//        assertSame("IG-88", updatedWebHook.getLastUpdatedUser());
//    }

}
