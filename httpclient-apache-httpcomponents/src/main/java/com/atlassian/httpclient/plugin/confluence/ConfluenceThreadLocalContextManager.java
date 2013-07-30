package com.atlassian.httpclient.plugin.confluence;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.RequestCacheThreadLocal;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.user.User;

import java.util.HashMap;
import java.util.Map;

public final class ConfluenceThreadLocalContextManager implements ThreadLocalContextManager<ConfluenceThreadLocalContextManager.ThreadLocalContext>
{
    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public ThreadLocalContext getThreadLocalContext()
    {
        return new ThreadLocalContext(AuthenticatedUserThreadLocal.getUser(), new HashMap<String, Object>(RequestCacheThreadLocal.getRequestCache()));
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    public void setThreadLocalContext(ThreadLocalContext context)
    {
        AuthenticatedUserThreadLocal.setUser(context.user);
        RequestCacheThreadLocal.setRequestCache(context.requestCache);
    }

    /**
     * Clear the thread local context on the current thread
     */
    public void resetThreadLocalContext()
    {
        AuthenticatedUserThreadLocal.reset();
        RequestCacheThreadLocal.clearRequestCache();
    }

    public static final class ThreadLocalContext
    {
        final User user;
        final Map<String, Object> requestCache;

        ThreadLocalContext(User user, Map<String, Object> requestCache)
        {
            this.user = user;
            this.requestCache = requestCache;
        }
    }
}
