package com.atlassian.httpclient.api.factory;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.atlassian.util.concurrent.NotNull;

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
    @NotNull HttpClient create(@NotNull HttpClientOptions options);

    /**
     * Creates a new instance of {@link com.atlassian.httpclient.api.HttpClient}
     *
     * @param options The http client options.  Cannot be null.
     * @param threadLocalContextManager the manager for thread local variables. Cannot be null.
     * @return The new instance.  Will never be null
     * @see #create(HttpClientOptions)
     */
    @NotNull HttpClient create(@NotNull HttpClientOptions options, @NotNull ThreadLocalContextManager threadLocalContextManager);

    /**
     * Disposes the given instance of {@link com.atlassian.httpclient.api.HttpClient}
     * @param httpClient The httpClient to dispose. Cannot be null.
     * @throws Exception in case of shutdown errors.
     */
    void dispose(@NotNull HttpClient httpClient) throws Exception;
}
