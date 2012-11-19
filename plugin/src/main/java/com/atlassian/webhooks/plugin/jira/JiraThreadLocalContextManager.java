package com.atlassian.webhooks.plugin.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import static com.google.common.base.Preconditions.*;

public final class JiraThreadLocalContextManager implements ThreadLocalContextManager<JiraThreadLocalContextManager.JiraThreadLocalContext>
{
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraThreadLocalContextManager(JiraAuthenticationContext authenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
        this.velocityRequestContextFactory = checkNotNull(velocityRequestContextFactory);
    }

    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public JiraThreadLocalContext getThreadLocalContext()
    {
        return new JiraThreadLocalContext(authenticationContext.getLoggedInUser(), velocityRequestContextFactory.getJiraVelocityRequestContext());
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    public void setThreadLocalContext(JiraThreadLocalContext context)
    {
        authenticationContext.setLoggedInUser(context.getUser());
        velocityRequestContextFactory.setVelocityRequestContext(context.getVelocityRequestContext());
    }

    /**
     * Clear the thread local context on the current thread
     */
    public void resetThreadLocalContext()
    {
        velocityRequestContextFactory.clearVelocityRequestContext();
        authenticationContext.setLoggedInUser(null);
    }

    public static class JiraThreadLocalContext
    {
        private final User user;
        private final VelocityRequestContext velocityRequestContext;

        private JiraThreadLocalContext(User user, VelocityRequestContext velocityRequestContext)
        {
            this.user = user;
            this.velocityRequestContext = velocityRequestContext;
        }

        public User getUser()
        {
            return user;
        }

        public VelocityRequestContext getVelocityRequestContext()
        {
            return velocityRequestContext;
        }
    }
}
