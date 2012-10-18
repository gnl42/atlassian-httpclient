package com.atlassian.httpclient.api;

import com.google.common.base.Function;

/**
 * A specific type of BaseResponsePromise for handling a response promise
 * for a single HTTP request
 */
public interface ResponsePromise extends BaseResponsePromise<Response>
{
    /**
     * Helps transforming this response promise into a new promise using {@link Function} to transform response into a
     * new {@code T}.
     *
     * @param <T> the type of the expected object once transformed.
     * @return a {@link com.atlassian.util.concurrent.Promise<T>}
     */
    public <T> ResponsePromiseTransformationBuilder<T> transform();
}
