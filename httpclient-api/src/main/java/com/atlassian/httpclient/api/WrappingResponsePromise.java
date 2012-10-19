package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.util.concurrent.ListenableFuture;

final class WrappingResponsePromise extends ForwardingPromise<Response> implements ResponsePromise
{
    private final Promise<Response> delegate;

    public WrappingResponsePromise(ListenableFuture<Response> delegate)
    {
        this.delegate = Promises.forListenableFuture(delegate);
    }

    @Override
    protected final Promise<Response> delegate()
    {
        return delegate;
    }

    @Override
    public <T> ResponseTransformationPromise<T> transform()
    {
        return new DefaultResponseTransformationPromise<T>(this);
    }
}
