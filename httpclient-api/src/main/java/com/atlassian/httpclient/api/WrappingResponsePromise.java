package com.atlassian.httpclient.api;

import io.atlassian.util.concurrent.Promise;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class WrappingResponsePromise implements ResponsePromise {
    private final Promise<Response> delegate;

    WrappingResponsePromise(Promise<Response> delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public Response claim() {
        return delegate.claim();
    }

    @Override
    public Promise<Response> done(Consumer<? super Response> consumer) {
        return delegate.done(consumer);
    }

    @Override
    public Promise<Response> fail(Consumer<Throwable> consumer) {
        return delegate.fail(consumer);
    }

    @Override
    public <B> Promise<B> flatMap(Function<? super Response, ? extends Promise<? extends B>> function) {
        return delegate.flatMap(function);
    }

    @Override
    public <B> Promise<B> fold(Function<Throwable, ? extends B> function,
                               Function<? super Response, ? extends B> function1) {
        return delegate.fold(function, function1);
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public Response get(long timeout, @Nonnull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public <B> Promise<B> map(Function<? super Response, ? extends B> function) {
        return delegate.map(function);
    }

    @Override
    public Promise<Response> recover(Function<Throwable, ? extends Response> function) {
        return delegate.recover(function);
    }

    @Override
    public Promise<Response> then(TryConsumer<? super Response> tryConsumer) {
        return delegate.then(tryConsumer);
    }

    public <T> Promise<T> transform(ResponseTransformation<T> transformation) {
        return delegate.fold(transformation.getFailFunction(), transformation.getSuccessFunctions());
    }
}
