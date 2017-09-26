package com.atlassian.httpclient.api;

import com.atlassian.httpclient.api.factory.HttpClientOptions;

/**
 * Thrown to indicate that a response was dropped because it contained an entity that was larger than the
 * {@link HttpClientOptions#getMaxEntitySize() configured maximum size}.
 * <p>
 * Contains the {@link #getResponse() response} with the response headers, status and first
 * {@link HttpClientOptions#getMaxCacheEntries() maxEntrySize} bytes of the response body.
 *
 * @since 0.23.5
 */
public class ResponseTooLargeException extends RuntimeException {

    static final long serialVersionUID = 1L;

    private final Response response;

    public ResponseTooLargeException(Response response, String message) {
        super(message);
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
