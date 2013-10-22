package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;

import static com.google.common.base.Preconditions.checkNotNull;

final class WrappingResponsePromise extends ForwardingPromise<Response> implements ResponsePromise
{
    private final Promise<Response> delegate;

    WrappingResponsePromise(Promise<Response> delegate)
    {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    protected final Promise<Response> delegate()
    {
        return delegate;
    }

    public <T> Promise<T> transform(ResponseTransformation<T> transformation)
    {
        return delegate.fold(transformation.getFailFunction(), transformation.getSuccessFunctions());
    }
}
