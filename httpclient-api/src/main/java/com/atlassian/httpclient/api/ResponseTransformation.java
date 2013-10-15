package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

/**
 * Transforms the {@link ResponsePromise} into a target object, allowing for transforming functions based on different
 * HTTP codes or exceptions. Under the covers, all functions are used in a fold() call.
 *
 * @param <T> The target object for the transformation.
 */
public interface ResponseTransformation<T>
{
    Function<Throwable, ? extends T> getFailFunction();

    Function<Response, T> getSuccessFunctions();

    /**
     * Converts the transformation into a promise for further mapping
     *
     * @return A promise that will return the object
     */
    Promise<T> apply(ResponsePromise responsePromise);

    interface Builder<T> extends Buildable<ResponseTransformation<T>>
    {

        /**
         * Register a function to transform HTTP responses with a specific status.
         * Use this as a fallback if the status code you're interested in does not have
         * a more explicit registration method for it.
         *
         * @param status The HTTP status to select on
         * @param f The transformation function
         * @return This instance for chaining
         * @see #on(int, com.google.common.base.Function)
         */
        Builder<T> on(HttpStatus status, Function<Response, ? extends T> f);

        /**
         * <p>Register a function to transform HTTP responses with a specific status code.
         * Use this as a fallback if the status code you're interested in does not have
         * a more explicit registration method for it.
         * <p>Prefer the {@link #on(HttpStatus, com.google.common.base.Function)} method if you're using <em>standard</em>
         * HTTP status.
         *
         * @param statusCode The code to select on
         * @param f The transformation function
         * @return This instance for chaining
         * @see #on(HttpStatus, com.google.common.base.Function)
         */
        Builder<T> on(int statusCode, Function<Response, ? extends T> f);

        // Informational (1xx) Selectors

        /**
         * Register a function to transform 'informational' (1xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> informational(Function<Response, ? extends T> f);

        // Successful (2xx) Selectors

        /**
         * Register a function to transform 'successful' (2xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> successful(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'ok' (200) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> ok(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'created' (201) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> created(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'no content' (204) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> noContent(Function<Response, ? extends T> f);

        // Redirection (3xx) Selectors

        /**
         * Register a function to transform 'redirection' (3xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> redirection(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'see other' (303) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> seeOther(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'not modified' (304) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> notModified(Function<Response, ? extends T> f);

        // Client Error (4xx) Selectors

        /**
         * Register a function to transform 'client error' (4xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> clientError(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'bad request' (400) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> badRequest(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'unauthorized' (401) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> unauthorized(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'forbidden' (403) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> forbidden(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'not found' (404) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> notFound(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'conflict' (409) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> conflict(Function<Response, ? extends T> f);

        // Server Error (5xx) Selectors

        /**
         * Register a function to transform 'server error' (5xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> serverError(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'internal server error' (500) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> internalServerError(Function<Response, ? extends T> f);

        /**
         * Register a function to transform 'service unavailable' (503) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> serviceUnavailable(Function<Response, ? extends T> f);

        // Aggregate Selectors

        /**
         * Register a function to transform all error (4xx and 5xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> error(Function<Response, ? extends T> f);

        /**
         * Register a function to transform all non-'successful' (1xx, 3xx, 4xx, 5xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> notSuccessful(Function<Response, ? extends T> f);

        /**
         * Register a function to transform all other HTTP responses (i.e. those not explicitly registered for).
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> others(Function<Response, ? extends T> f);

        /**
         * Register a function to transform both of the following events:
         * <ul>
         *     <li>Any value passed to <code>fail()</code></li>
         *     <li>Any value passed to others(), converted into an exception</li>
         * </ul>
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> otherwise(Function<Throwable, T> f);

        /**
         * Register a function to transform all completed (1xx, 2xx, 3xx, 4xx, and 5xx) HTTP responses.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> done(Function<Response, T> f);

        // Exception Selectors

        /**
         * Register a function to transform exceptions thrown while executing the HTTP request.
         *
         * @param f The transformation function
         * @return This instance for chaining
         */
        Builder<T> fail(Function<Throwable, ? extends T> f);
    }
}
