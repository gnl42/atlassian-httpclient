package com.atlassian.httpclient.api.factory;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;

/**
 * Creates configured instances of {@link com.atlassian.httpclient.api.HttpClient}
 */
public interface HttpClientFactory
{
    /**
     * Creates a new instance of {@link com.atlassian.httpclient.api.HttpClient}
     *
     * @param options The http client options.  Cannot be null.
     * @return The new instance.  Will never be null
     * @see #create(HttpClientOptions, ThreadLocalContextManager)
     */
    HttpClient create(HttpClientOptions options);

    /**
     * Creates a new instance of {@link com.atlassian.httpclient.api.HttpClient}
     *
     * @param options The http client options.  Cannot be null.
     * @param threadLocalContextManager the manager for thread local variables. Cannot be null.
     * @return The new instance.  Will never be null
     * @see #create(HttpClientOptions)
     */
    HttpClient create(HttpClientOptions options, ThreadLocalContextManager<?> threadLocalContextManager);
}
