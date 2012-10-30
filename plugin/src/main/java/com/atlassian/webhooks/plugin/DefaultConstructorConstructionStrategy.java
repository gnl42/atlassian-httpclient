package com.atlassian.webhooks.plugin;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public final class DefaultConstructorConstructionStrategy implements ConstructionStrategy
{
    @Override
    public <T> T get(Class<T> type)
    {
        try
        {
            return Preconditions.checkNotNull(type).newInstance();
        }
        catch (InstantiationException e)
        {
            throw Throwables.propagate(e);
        }
        catch (IllegalAccessException e)
        {
            throw Throwables.propagate(e);
        }
    }
}
