package com.atlassian.webhooks.plugin.test.jira;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.webhooks.plugin.test.CheckThreadContext;

import static com.google.common.base.Preconditions.*;
import static org.junit.Assert.*;

public final class CheckThreadContextJira implements CheckThreadContext
{
    private final JiraAuthenticationContext authenticationContext;

    public CheckThreadContextJira(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
    }

    @Override
    public void before()
    {
        authenticationContext.setLoggedInUser(USER);
    }

    @Override
    public void check() throws AssertionError
    {
        assertSame(USER, authenticationContext.getLoggedInUser());
    }

    private static final User USER = new User()
    {
        @Override
        public String getFirstName()
        {
            return null;
        }

        @Override
        public String getLastName()
        {
            return null;
        }

        @Override
        public long getDirectoryId()
        {
            return 0;
        }

        @Override
        public boolean isActive()
        {
            return false;
        }

        @Override
        public String getEmailAddress()
        {
            return null;
        }

        @Override
        public String getDisplayName()
        {
            return null;
        }

        @Override
        public int compareTo(com.atlassian.crowd.embedded.api.User user)
        {
            return 0;
        }

        @Override
        public String getName()
        {
            return null;
        }
    };
}
