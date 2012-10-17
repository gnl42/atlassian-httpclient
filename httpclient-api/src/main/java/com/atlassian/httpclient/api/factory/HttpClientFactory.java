package com.atlassian.httpclient.api.factory;

import com.atlassian.httpclient.api.HttpClient;

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
     */
    HttpClient create(HttpClientOptions options);
}
