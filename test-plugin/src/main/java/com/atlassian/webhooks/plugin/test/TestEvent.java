package com.atlassian.webhooks.plugin.test;

import static com.google.common.base.Preconditions.*;

public final class TestEvent
{
    public final String value;

    public TestEvent(String value)
    {
        this.value = checkNotNull(value);
    }
}
