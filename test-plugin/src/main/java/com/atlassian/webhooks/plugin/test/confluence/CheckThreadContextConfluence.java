package com.atlassian.webhooks.plugin.test.confluence;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.User;
import com.atlassian.webhooks.plugin.test.CheckThreadContext;

import static org.junit.Assert.*;

public final class CheckThreadContextConfluence implements CheckThreadContext
{
    @Override
    public void before()
    {
        AuthenticatedUserThreadLocal.setUser(USER);
    }

    @Override
    public void check() throws AssertionError
    {
        assertSame(USER, AuthenticatedUserThreadLocal.getUser());
    }

    private static final User USER = new User()
    {
        @Override
        public String getFullName()
        {
            return null;
        }

        @Override
        public String getEmail()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }
    };
}
