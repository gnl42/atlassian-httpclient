package com.atlassian.webhooks.plugin.junit;

import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.api.provider.WebHookListenerServiceResponse;
import com.atlassian.webhooks.plugin.AnnotatedEvent;
import com.atlassian.webhooks.plugin.test.EventWithPersistentListener;
import com.atlassian.webhooks.plugin.test.ParameterizedEvent;
import com.atlassian.webhooks.plugin.test.ServiceAccessor;
import com.atlassian.webhooks.plugin.test.TestEvent;
import com.atlassian.webhooks.plugin.test.WebHookServlet;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class TestWebHookOnEvents
{
    @Test
    public void testPluginEnabledWebHook() throws Exception
    {
        final WebHookServlet.Hook hook = WebHookServlet.waitAndPopPluginEnabled();
        assertNotNull(hook);
        assertFalse(WebHookServlet.hasPluginEnabledHooks());
    }

    @Test
    public void testWebHookConfiguredFromProvider() throws Exception
    {
        assertFalse(WebHookServlet.hasHooks());

        final String eventValue = "This is my value!";
        ServiceAccessor.eventPublisher.publish(new TestEvent(eventValue));

        final WebHookServlet.Hook hook = WebHookServlet.waitAndPop();
        assertNotNull(hook);
        assertTrue(hook.body.contains(eventValue));
        assertFalse(WebHookServlet.hasHooks());
    }

    @Test
    public void testWebHookConfiguredWithAnnotation() throws Exception
    {
        assertFalse(WebHookServlet.hasHooks());

        final String eventValue = "This is my _annotated_ value!";
        ServiceAccessor.eventPublisher.publish(new AnnotatedEvent(eventValue));

        final WebHookServlet.Hook hook = WebHookServlet.waitAndPop();
        assertNotNull(hook);
        assertTrue(hook.body.contains(eventValue));
        assertFalse(WebHookServlet.hasHooks());
    }

    @Test
    public void testWebHookWithParams() throws Exception
    {
        assertFalse(WebHookServlet.hasHooks());
        ServiceAccessor.eventPublisher.publish(new ParameterizedEvent("true"));
        final WebHookServlet.Hook hook = WebHookServlet.waitAndPop();
        assertNotNull(hook);
        assertTrue(hook.body.contains("true"));
        assertFalse(WebHookServlet.hasHooks());

        ServiceAccessor.eventPublisher.publish(new ParameterizedEvent("false"));
        assertNull(WebHookServlet.waitAndPop());
    }

    @Test
    public void testPersistentWebHook() throws InterruptedException, IOException
    {
        assertFalse(WebHookServlet.hasHooks());
        registerWebHook("/plugins/servlet/webhooks-test/persistent_event", "persitent_webhook_listener");//, "{\"qualification\":true, \"secondaryKey\":\"some_event_value\"}");

        ServiceAccessor.eventPublisher.publish(new EventWithPersistentListener("true", "some_event_value"));
        final WebHookServlet.Hook hook = WebHookServlet.waitAndPopPersistentEventWebHooks();
        assertNotNull(hook);
        assertTrue(hook.body.contains("some_event_value"));
    }

    private void registerWebHook(String url, String name) throws IOException
    {
        final WebHookListenerServiceResponse webHookListenerServiceResponse = ServiceAccessor.webHookListenerService.registerWebHookListener(
                new WebHookListenerParameters.WebHookListenerParametersImpl(0, true, new Date(), "admin", name, url,
                        ImmutableMap.<String, Object>of("qualification", true, "secondaryKey", "some_event_value"),
                        Lists.newArrayList("jira:issue_updated"), WebHookListenerService.RegistrationMethod.SERVICE.name())
        );
        assertNotNull(webHookListenerServiceResponse);

    }
}
