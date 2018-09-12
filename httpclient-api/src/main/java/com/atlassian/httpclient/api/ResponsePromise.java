package com.atlassian.httpclient.api;

import io.atlassian.util.concurrent.Promise;

import java.util.function.Function;

/**
 * A specific type of Promise for transforming a promise with a response into another object
 * with functions for different HTTP codes and situations
 */
public interface ResponsePromise extends Promise<Response> {
    /**
     * Helps transforming this response promise into a new promise using {@link Function} to transform response into a
     * new {@code T}.
     *
     * @param <T> the type of the expected object once transformed.
     * @return a {@link Promise<T>}
     */
    <T> Promise<T> transform(ResponseTransformation<T> transformation);
}
