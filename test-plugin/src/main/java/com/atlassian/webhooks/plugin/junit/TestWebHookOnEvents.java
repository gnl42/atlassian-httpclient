package com.atlassian.webhooks.plugin.junit;

import com.atlassian.webhooks.plugin.AnnotatedEvent;
import com.atlassian.webhooks.plugin.test.ParameterizedEvent;
import com.atlassian.webhooks.plugin.test.ServiceAccessor;
import com.atlassian.webhooks.plugin.test.TestEvent;
import com.atlassian.webhooks.plugin.test.WebHookServlet;
import org.junit.Test;

import static org.junit.Assert.*;

public final class TestWebHookOnEvents
{
    @Test
    public void testPluginEnabledWebHook() throws Exception
    {
        final WebHookServlet.Hook hook = WebHookServlet.waitAndPopPluginEnabled();
        assertNotNull(hook);
        assertFalse(WebHookServlet.hasPluginEnabledHooks());
//        assertTrue(hook.body.contains(eventValue));
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
}
