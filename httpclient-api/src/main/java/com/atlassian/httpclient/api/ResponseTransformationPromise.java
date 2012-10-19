package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

/**
 * Promise that allows for transforming functions based on different HTTP codes or exceptions.
 * Under the covers, all functions are used in a fold() call.
 *
 * @param <O> The target object for the transformation.
 */
public interface ResponseTransformationPromise<O> extends Promise<O>
{
    // Custom Selectors

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
    ResponseTransformationPromise<O> on(HttpStatus status, Function<Response, ? extends O> f);

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
    ResponseTransformationPromise<O> on(int statusCode, Function<Response, ? extends O> f);

    // Informational (1xx) Selectors

    /**
     * Register a function to transform 'informational' (1xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> informational(Function<Response, ? extends O> f);

    // Successful (2xx) Selectors

    /**
     * Register a function to transform 'successful' (2xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> successful(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'ok' (200) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> ok(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'created' (201) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> created(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'no content' (204) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> noContent(Function<Response, ? extends O> f);

    // Redirection (3xx) Selectors

    /**
     * Register a function to transform 'redirection' (3xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> redirection(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'see other' (303) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> seeOther(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'not modified' (304) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> notModified(Function<Response, ? extends O> f);

    // Client Error (4xx) Selectors

    /**
     * Register a function to transform 'client error' (4xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> clientError(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'bad request' (400) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> badRequest(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'unauthorized' (401) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> unauthorized(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'forbidden' (403) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> forbidden(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'not found' (404) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> notFound(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'conflict' (409) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> conflict(Function<Response, ? extends O> f);

    // Server Error (5xx) Selectors

    /**
     * Register a function to transform 'server error' (5xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> serverError(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'internal server error' (500) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> internalServerError(Function<Response, ? extends O> f);

    /**
     * Register a function to transform 'service unavailable' (503) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> serviceUnavailable(Function<Response, ? extends O> f);

    // Aggregate Selectors

    /**
     * Register a function to transform all error (4xx and 5xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> error(Function<Response, ? extends O> f);

    /**
     * Register a function to transform all non-'successful' (1xx, 3xx, 4xx, 5xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> notSuccessful(Function<Response, ? extends O> f);

    /**
     * Register a function to transform all other HTTP responses (i.e. those not explicitly registered for).
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> others(Function<Response, ? extends O> f);

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
    ResponseTransformationPromise<O> otherwise(Function<Throwable, O> f);

    /**
     * Register a function to transform all completed (1xx, 2xx, 3xx, 4xx, and 5xx) HTTP responses.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> done(Function<Response, O> f);
    
    // Exception Selectors

    /**
     * Register a function to transform exceptions thrown while executing the HTTP request.
     *
     * @param f The transformation function
     * @return This instance for chaining
     */
    ResponseTransformationPromise<O> fail(Function<Throwable, ? extends O> f);
}
