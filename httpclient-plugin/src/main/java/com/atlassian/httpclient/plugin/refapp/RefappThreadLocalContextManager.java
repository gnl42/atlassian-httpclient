package com.atlassian.httpclient.plugin.refapp;

import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.seraph.auth.AuthenticationContext;

import java.security.Principal;

public final class RefappThreadLocalContextManager implements ThreadLocalContextManager<Principal>
{
    private final AuthenticationContext authenticationContext;

    public RefappThreadLocalContextManager(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public Principal getThreadLocalContext()
    {
        return authenticationContext.getUser();
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    public void setThreadLocalContext(Principal context)
    {
        authenticationContext.setUser(context);
    }

    /**
     * Clear the thread local context on the current thread
     */
    public void resetThreadLocalContext()
    {
        authenticationContext.setUser(null);
    }
}
