package com.atlassian.webhooks.plugin.junit;

import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.webhooks.plugin.AnnotatedEvent;
import com.atlassian.webhooks.plugin.test.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xml.security.utils.Base64;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

    @Test
    public void testPersistentWebHook() throws InterruptedException, IOException
    {
        assertFalse(WebHookServlet.hasHooks());
        registerWebHook("http://localhost:5990/refapp/plugins/servlet/webhook/persistent_event", "persitent_webhook_listener", "true:some_event_value");

        ServiceAccessor.eventPublisher.publish(new EventWithPersistentListener("true", "some_event_value"));
        final WebHookServlet.Hook hook = WebHookServlet.waitAndPopPersistentEventWebHooks();
        assertNotNull(hook);
        assertTrue(hook.body.contains("some_event_value"));
    }

    private void registerWebHook(String url, String name, String parameters) throws IOException
    {
        HttpPost request = new HttpPost();
        request.setEntity(new StringEntity("{ \"name\": \"" + name + "\", \"url\": \"" + url + "\", \"events\": \"jira:issue_updated\", \"parameters\": \"" + parameters + "\"}"));
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Basic " + Base64.encode("admin:admin".getBytes()));
        HttpResponse response = new DefaultHttpClient().execute(request);
        assertEquals(201, response.getStatusLine().getStatusCode());
    }
}
