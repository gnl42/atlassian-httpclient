package com.atlassian.webhooks.plugin.test.refapp;

import com.atlassian.seraph.auth.AuthenticationContext;
import com.atlassian.webhooks.plugin.test.CheckThreadContext;

import java.security.Principal;

import static com.google.common.base.Preconditions.*;
import static org.junit.Assert.assertSame;

public final class CheckThreadContextRefapp implements CheckThreadContext
{
    private final AuthenticationContext authenticationContext;

    public CheckThreadContextRefapp(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
    }

    @Override
    public void before()
    {
        authenticationContext.setUser(USER);
    }

    @Override
    public void check() throws AssertionError
    {
        assertSame(USER, authenticationContext.getUser());
    }

    public static final Principal USER = new Principal()
    {
        @Override
        public String getName()
        {
            return null;
        }
    };
}
