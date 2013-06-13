package com.atlassian.webhooks.plugin.test;

import com.google.common.io.CharStreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public final class WebHookServlet extends HttpServlet
{
    static volatile BlockingDeque<Hook> pluginEnabledHook = new LinkedBlockingDeque<Hook>();
    static volatile BlockingDeque<Hook> hooks = new LinkedBlockingDeque<Hook>();
    static volatile BlockingDeque<Hook> persistentEventsHook;

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final Hook hook = new Hook(CharStreams.toString(req.getReader()));
        if (req.getPathInfo().endsWith("plugin_enabled"))
        {
            pluginEnabledHook.push(hook);
        }
        else if (req.getPathInfo().endsWith("persistent_event"))
        {
            persistentEventsHook.push(hook);
        }
        else
        {
            push(hook);
        }
    }

    static void push(Hook hook)
    {
        hooks.add(hook);
    }

    public static Hook waitAndPop() throws InterruptedException
    {
        return hooks.poll(5, TimeUnit.SECONDS);
    }

    public static Hook waitAndPopPluginEnabled() throws InterruptedException
    {
        return pluginEnabledHook.poll(5, TimeUnit.SECONDS);
    }

    public static Hook waitAndPopPersistentEventWebHooks() throws InterruptedException
    {
        return persistentEventsHook.poll(5, TimeUnit.SECONDS);
    }

    public static boolean hasHooks()
    {
        return !hooks.isEmpty();
    }

    public static boolean hasPluginEnabledHooks()
    {
        return !pluginEnabledHook.isEmpty();
    }

    public static class Hook
    {
        public final String body;

        Hook(String body)
        {
            this.body = body;
        }
    }
}
