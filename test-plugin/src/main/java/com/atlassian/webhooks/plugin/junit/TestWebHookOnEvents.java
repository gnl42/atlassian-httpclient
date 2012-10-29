package com.atlassian.webhooks.plugin.junit;

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
}
