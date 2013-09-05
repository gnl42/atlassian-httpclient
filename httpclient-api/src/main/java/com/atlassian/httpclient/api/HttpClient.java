package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;

/**
 * A service providing asynchronous HTTP request creation and execution.
 * <p/>
 * To use this service, first create a {@link Request} instance with one of the <code>newRequest()</code> methods. Then,
 * populate the request with any additional options, and finally call one of its HTTP verb methods to execute the
 * request. See the {@link Request} class for finer control over the construction of the HTTP requests to be executed.
 */
public interface HttpClient extends RequestFactory
{
    /**
     * Executes this request through the {@link HttpClient} service.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    Promise<Response> execute(Request request);

    Builders builders();

    /**
     * Flush the cache entries by matching the URI using a regular expression
     *
     * @param uriPattern The regular expression to match
     *
     * TODO internalise, shouldn't be API
     */
    // void flushCacheByUriPattern(Pattern uriPattern);
}
