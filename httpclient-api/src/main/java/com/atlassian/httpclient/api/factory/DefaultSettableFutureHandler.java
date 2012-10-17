package com.atlassian.httpclient.api.factory;

import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;

/**
 * Simple handler that just creates and wraps a normal future
 */
final class DefaultSettableFutureHandler<V> implements SettableFutureHandler<V>
{
    private final SettableFuture<V> future = SettableFuture.create();

    @Override
    public boolean set(@Nullable V value)
    {
        return future.set(value);
    }

    @Override
    public boolean setException(Throwable throwable)
    {
        return future.setException(throwable);
    }

    @Override
    public SettableFuture<V> getFuture()
    {
        return future;
    }
}
