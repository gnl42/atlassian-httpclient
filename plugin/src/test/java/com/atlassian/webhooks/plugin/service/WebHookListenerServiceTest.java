package com.atlassian.webhooks.plugin.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.provider.WebHookListenerServiceResponse;
import com.atlassian.webhooks.api.provider.event.WebHookCreatedEvent;
import com.atlassian.webhooks.api.provider.event.WebHookDeletedEvent;
import com.atlassian.webhooks.api.provider.event.WebHookDisabledEvent;
import com.atlassian.webhooks.api.provider.event.WebHookEnabledEvent;
import com.atlassian.webhooks.plugin.PluginProperties;
import com.atlassian.webhooks.plugin.ao.WebHookListenerAO;
import com.atlassian.webhooks.plugin.api.WebHookListenerServiceImpl;
import com.atlassian.webhooks.plugin.event.WebHookEventDispatcher;
import com.atlassian.webhooks.plugin.rest.WebHookListenerRegistration;
import com.atlassian.webhooks.plugin.store.WebHookListenerCachingStore;
import com.atlassian.webhooks.plugin.store.WebHookListenerStore;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.java.ao.EntityManager;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.symmetricDifference;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ActiveObjectsJUnitRunner.class)
public class WebHookListenerServiceTest
{
    public static final String LAST_UPDATED_USER = "Bobba Fett";
    public static final String WEBHOOK_NAME = "We listen for news about capturing of Han Solo";
    private static final String ILLEGAL_WEBHOOK_NAME = "We listen for news about Han Solo rescue";
    private static final String PERMANENT_WEBHOOK_NAME = "Rebel victory webhook";
    public static final String TARGET_URL = "http://www.superstar-destroyer.com.empire";
    public static final Iterable<String> EVENTS = newArrayList("rebel_captured_event");
    public static final String PARAMETERS = "rebel = 'Han Solo'";

    @SuppressWarnings ("UnusedDeclaration")
    private EntityManager entityManager;
    private WebHookListenerServiceImpl webHookListenerService;
    private UserManager userManager;
    private EventPublisher eventPublisher;

    @Before
    public void setUp()
    {
        this.userManager = mock(UserManager.class);
        when(userManager.getRemoteUsername()).thenReturn(LAST_UPDATED_USER);
        this.eventPublisher = mock(EventPublisher.class);
        ActiveObjects ao = new TestActiveObjects(entityManager);
        WebHookListenerStore webHookListenerStore = new WebHookListenerStore(ao, userManager, mock(I18nResolver.class));
        WebHookListenerCachingStore webHookListenerCachingStore = new WebHookListenerCachingStore(webHookListenerStore);
        WebHookEventDispatcher webHookEventDispatcher = new WebHookEventDispatcher(eventPublisher);

        this.webHookListenerService = new WebHookListenerServiceImpl(
                webHookListenerCachingStore, new Validator(), webHookEventDispatcher, mock(I18nResolver.class));
        //noinspection unchecked
        ao.migrate(WebHookListenerAO.class);

        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PluginProperties.PLUGIN_KEY);
        webHookListenerCachingStore.onPluginStarted(new PluginEnabledEvent(plugin));
    }

    @Test
    public void testWebHookRegistration()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        final WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);

        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertEquals(WEBHOOK_NAME, webHookListenerServiceResponse.getRegisteredListener().get().getName());

        assertThat(webHookListenerService.getAllWebHookListeners(), new ContainsWebHookListenerMatcher(webHookListenerParameters));
    }

    @Test
    public void testWebHookRegistrationAndUpdate()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);

        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertEquals(WEBHOOK_NAME, webHookListenerServiceResponse.getRegisteredListener().get().getName());
        Integer id = webHookListenerServiceResponse.getRegisteredListener().get().getId();

        WebHookListenerRegistrationParameters updateWebHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, newArrayList("rebel_lost_event"), true);
        webHookListenerServiceResponse = webHookListenerService.updateWebHookListener(id, updateWebHookListenerParameters);
        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertEquals(WEBHOOK_NAME, webHookListenerServiceResponse.getRegisteredListener().get().getName());
        assertEquals(id, webHookListenerServiceResponse.getRegisteredListener().get().getId());

        assertThat("Service should contain updated listener", webHookListenerService.getAllWebHookListeners(), new ContainsWebHookListenerMatcher(updateWebHookListenerParameters));
        assertThat("Service shouldn't contain the first version of listener", webHookListenerService.getAllWebHookListeners(), CoreMatchers.not(new ContainsWebHookListenerMatcher(webHookListenerParameters)));
    }

    @Test
    public void testUpdateOfWebHookSetsUserName()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);

        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertEquals(LAST_UPDATED_USER, webHookListenerServiceResponse.getRegisteredListener().get().getLastUpdatedUser());

        when(userManager.getRemoteUsername()).thenReturn("IG-88");
        webHookListenerServiceResponse = webHookListenerService.updateWebHookListener(webHookListenerServiceResponse.getRegisteredListener().get().getId(), webHookListenerParameters);

        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertEquals("IG-88", webHookListenerServiceResponse.getRegisteredListener().get().getLastUpdatedUser());
    }

    @Test
    public void testWebHookCreatedEventRaised()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        webHookListenerService.registerWebHookListener(webHookListenerParameters);
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookCreatedEvent.class)));
    }

    @Test
    public void testDeletingWebHookListener()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);

        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookCreatedEvent.class)));

        final MessageCollection messageCollection = webHookListenerService.deleteWebHookListener(webHookListenerServiceResponse.getRegisteredListener().get().getId());
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookDeletedEvent.class)));
        assertTrue("Deleting listener didn't create any error messages", messageCollection.isEmpty());
        assertThat(Lists.newArrayList(webHookListenerService.getAllWebHookListeners()), IsCollectionWithSize.hasSize(0));
    }

    @Test
    public void testDisablingAndEnablingWebHookListener()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);
        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());
        assertTrue("Registered listener is enabled", webHookListenerServiceResponse.getRegisteredListener().get().isEnabled());
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookCreatedEvent.class)));

        Optional<WebHookListenerParameters> disabledWebHookListener = webHookListenerService.enableWebHookListener(webHookListenerServiceResponse.getRegisteredListener().get().getId(), false);
        assertTrue(disabledWebHookListener.isPresent());
        assertFalse("WebHook Listener is disabled", disabledWebHookListener.get().isEnabled());
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookDisabledEvent.class)));

        Optional<WebHookListenerParameters> updatedWebHookListener = webHookListenerService.enableWebHookListener(disabledWebHookListener.get().getId(), true);
        assertTrue(updatedWebHookListener.isPresent());
        assertTrue("WebHook Listener is disabled", updatedWebHookListener.get().isEnabled());
        verify(eventPublisher, times(1)).publish(argThat(new WebHookEventFiredMatcher(WebHookEnabledEvent.class)));
    }

    @Test
    public void testCreatingWebHookWithIllegalParameters()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(ILLEGAL_WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);
        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        final MessageCollection messageCollection = webHookListenerServiceResponse.getMessageCollection();
        assertFalse("Adding new listener created an error messages", messageCollection.isEmpty());
        assertEquals(messageCollection.getMessages().get(0).getKey(), "illegal.name");
    }

    @Test
    public void testDeletingWebHookWithIllegalParameters()
    {
        WebHookListenerRegistrationParameters webHookListenerParameters = new WebHookListenerRegistration(PERMANENT_WEBHOOK_NAME, TARGET_URL, PARAMETERS, EVENTS, true);
        WebHookListenerServiceResponse webHookListenerServiceResponse = webHookListenerService.registerWebHookListener(webHookListenerParameters);
        assertNotNull("Response from service can't be null", webHookListenerServiceResponse);
        assertTrue("Adding new listener didn't create any error messages", webHookListenerServiceResponse.getMessageCollection().isEmpty());
        assertTrue("Registered listener was returned from service", webHookListenerServiceResponse.getRegisteredListener().isPresent());

        final MessageCollection messageCollection = webHookListenerService.deleteWebHookListener(webHookListenerServiceResponse.getRegisteredListener().get().getId());
        assertFalse("Removing new listener created an error messages", messageCollection.isEmpty());
        assertEquals(messageCollection.getMessages().get(0).getKey(), "cant.delete.webhook");

        assertThat(newArrayList(webHookListenerService.getAllWebHookListeners()), IsCollectionWithSize.hasSize(1));
    }

    private static class Validator implements WebHookListenerActionValidator
    {
        @Override
        public MessageCollection validateWebHookRegistration(final WebHookListenerRegistrationParameters registrationParameters)
        {
            if (registrationParameters.getName().equals(ILLEGAL_WEBHOOK_NAME))
            {
                return new ErrorMessageCollection("illegal.name");
            }
            return WebHookListenerActionValidator.ErrorMessageCollection.emptyErrorMessageCollection();
        }

        @Override
        public MessageCollection validateWebHookUpdate(final WebHookListenerRegistrationParameters registrationParameters)
        {
            return WebHookListenerActionValidator.ErrorMessageCollection.emptyErrorMessageCollection();
        }

        @Override
        public MessageCollection validateWebHookRemoval(final WebHookListenerParameters registrationParameters)
        {
            if (registrationParameters.getName().equals(PERMANENT_WEBHOOK_NAME))
            {
                return new ErrorMessageCollection("cant.delete.webhook");
            }
            return WebHookListenerActionValidator.ErrorMessageCollection.emptyErrorMessageCollection();
        }
    }

    private static class ContainsWebHookListenerMatcher extends TypeSafeMatcher<Iterable<WebHookListenerParameters>>
    {
        private final WebHookListenerRegistrationParameters webHookListenerParameters;

        public ContainsWebHookListenerMatcher(final WebHookListenerRegistrationParameters webHookListenerParameters)
        {
            this.webHookListenerParameters = webHookListenerParameters;
        }

        @Override
        public boolean matchesSafely(final Iterable<WebHookListenerParameters> webHookListenerParametersCollection)
        {
            return Iterables.find(webHookListenerParametersCollection, new Predicate<WebHookListenerParameters>()
            {
                @Override
                public boolean apply(final WebHookListenerParameters input)
                {
                    return input.getName().equals(webHookListenerParameters.getName())
                            && input.getUrl().equals(webHookListenerParameters.getUrl())
                            && input.getParameters().equals(webHookListenerParameters.getParameters())
                            && symmetricDifference(input.getEvents() != null ? copyOf(input.getEvents()) : of(), copyOf(webHookListenerParameters.getEvents())).isEmpty();
                }
            }, null) != null;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("Service doesn't contain webhook " + webHookListenerParameters);
        }
    }

    private class WebHookEventFiredMatcher extends TypeSafeMatcher<Object>
    {
        private final Class<?> webHookCreatedEventClass;

        public WebHookEventFiredMatcher(final Class<?> webHookCreatedEventClass)
        {
            this.webHookCreatedEventClass = webHookCreatedEventClass;
        }

        @Override
        public boolean matchesSafely(final Object o)
        {
            return webHookCreatedEventClass.isInstance(o);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("Event isn't a subclass of " + webHookCreatedEventClass.getName());
        }
    }
}
