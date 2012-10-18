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
    static volatile BlockingDeque<Hook> hooks = new LinkedBlockingDeque<Hook>();

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String body = CharStreams.toString(req.getReader());
        push(new Hook(body));
    }

    static void push(Hook hook)
    {
        hooks.add(hook);
    }

    public static Hook waitAndPop() throws InterruptedException
    {
        return hooks.poll(30, TimeUnit.SECONDS);
    }

    public static boolean hasHooks()
    {
        return !hooks.isEmpty();
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
