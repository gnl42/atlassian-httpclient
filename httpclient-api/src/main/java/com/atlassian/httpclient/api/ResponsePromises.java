package com.atlassian.httpclient.api;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;

/**
 * Helper methods for working with response promises
 */
public final class ResponsePromises
{
    private ResponsePromises() { }

    public static ResponsePromise toResponsePromise(ListenableFuture<Response> future)
    {
        return new WrappingResponsePromise(future);
    }

    public static <T> Function<Response, T> newUnexpectedResponseFunction()
    {
        return new Function<Response, T>()
        {
            @Override
            public T apply(@Nullable Response response)
            {
                throw new UnexpectedResponseException(response);
            }
        };
    }
}
