package com.atlassian.webhooks.plugin.test;

public interface CheckThreadContext
{
    void before();

    void check() throws AssertionError;
}
