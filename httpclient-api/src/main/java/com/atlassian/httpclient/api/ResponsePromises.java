package com.atlassian.httpclient.api;

import io.atlassian.util.concurrent.Promise;

import java.util.function.Function;

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
        return response -> {
            throw new UnexpectedResponseException(response);
        };
    }
}
