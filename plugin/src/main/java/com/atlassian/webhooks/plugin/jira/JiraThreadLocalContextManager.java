package com.atlassian.webhooks.plugin.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;

public final class JiraThreadLocalContextManager implements ThreadLocalContextManager<JiraThreadLocalContextManager.JiraThreadLocalContext>
{
    private final JiraAuthenticationContext authenticationContext;
    private final TenantReference tenantReference;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraThreadLocalContextManager(JiraAuthenticationContext authenticationContext, TenantReference tenantReference, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.authenticationContext = authenticationContext;
        this.tenantReference = tenantReference;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public JiraThreadLocalContext getThreadLocalContext()
    {
        return new JiraThreadLocalContext(authenticationContext.getLoggedInUser(), tenantReference.get(), velocityRequestContextFactory.getJiraVelocityRequestContext());
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    public void setThreadLocalContext(JiraThreadLocalContext context)
    {
        tenantReference.set(context.getTenant(), false);
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
        tenantReference.remove();
    }

    public static class JiraThreadLocalContext
    {
        private final User user;
        private final Tenant tenant;
        private final VelocityRequestContext velocityRequestContext;

        private JiraThreadLocalContext(User user, Tenant tenant, VelocityRequestContext velocityRequestContext)
        {
            this.user = user;
            this.tenant = tenant;
            this.velocityRequestContext = velocityRequestContext;
        }

        public User getUser()
        {
            return user;
        }

        public Tenant getTenant()
        {
            return tenant;
        }

        public VelocityRequestContext getVelocityRequestContext()
        {
            return velocityRequestContext;
        }
    }
}
