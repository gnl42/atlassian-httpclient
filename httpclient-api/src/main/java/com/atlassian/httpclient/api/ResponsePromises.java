package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Helper methods for working with response promises
 */
public final class ResponsePromises {
    private ResponsePromises() {
    }

    public static ResponsePromise toResponsePromise(Promise<Response> promise) {
        return new WrappingResponsePromise(promise);
    }

    public static <T> Function<Response, T> newUnexpectedResponseFunction() {
        return new Function<Response, T>() {
            @Override
            public T apply(@Nullable Response response) {
                throw new UnexpectedResponseException(response);
            }
        };
    }
}
