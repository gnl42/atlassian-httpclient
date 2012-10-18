package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 */
final class WrappingResponsePromise implements ResponsePromise
{
    private final Promise<Response> delegate;

    public WrappingResponsePromise(ListenableFuture<Response> delegate)
    {
        this.delegate = Promises.forListenableFuture(delegate);
    }

    @Override
    public <T> ResponseTransformationPromise<T> transform()
    {
        return new DefaultResponseTransformationPromise<T>(this);
    }

    @Override
    public Response claim()
    {
        return delegate.claim();
    }

    @Override
    public Promise<Response> done(Effect<Response> e)
    {
        return delegate.done(e);
    }

    @Override
    public Promise<Response> fail(Effect<Throwable> e)
    {
        return delegate.fail(e);
    }

    @Override
    public Promise<Response> then(FutureCallback<Response> callback)
    {
        return delegate.then(callback);
    }

    @Override
    public <B> Promise<B> map(Function<? super Response, ? extends B> function)
    {
        return delegate.map(function);
    }

    @Override
    public <B> Promise<B> flatMap(Function<? super Response, Promise<B>> function)
    {
        return delegate.flatMap(function);
    }

    @Override
    public Promise<Response> recover(Function<Throwable, ? extends Response> handleThrowable)
    {
        return delegate.recover(handleThrowable);
    }

    @Override
    public <B> Promise<B> fold(Function<Throwable, ? extends B> handleThrowable,
            Function<? super Response, ? extends B> function)
    {
        return delegate.fold(handleThrowable, function);
    }

    @Override
    public void addListener(Runnable listener, Executor executor)
    {
        delegate.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return delegate.isDone();
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException
    {
        return delegate.get();
    }

    @Override
    public Response get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException
    {
        return delegate.get(timeout, unit);
    }
}
