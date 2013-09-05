package com.atlassian.httpclient.api;

import java.net.URI;

/**
 * Convenient factory methods for creating a {@link Request.Builder} with common defaults.
 */
public interface RequestFactory
{

    /**
     * Constructs a new request. Sets the accept property to a default of "&#42;/&#42;".
     *
     * @return The new request object
     */
    Request.Builder newRequest();

    /**
     * Constructs a new Request with the specified URI. Sets the accept property to a default of "&#42;/&#42;".
     *
     * @param uri The endpoint URI for this request
     * @return The new request object
     */
    Request.Builder newRequest(URI uri);

    /**
     * Constructs a new Request with the specified URI. Sets the accept property to a default of "&#42;/&#42;".
     *
     * @param uri The endpoint URI for this request
     * @return The new request object
     */
    Request.Builder newRequest(String uri);

    /**
     * Constructs a new Request with the specified URI, contentType, and entity. Sets the accept property to a default of
     * "&#42;/&#42;", and the content charset property to "UTF-8". This should only be used for sending textual content
     * types, typically via the POST or PUT HTTP methods.
     *
     * @param uri The endpoint URI for this request
     * @param contentType A textual IANA media type
     * @param entity A string entity to send as this request's message body
     * @return The new request object
     */
    Request.Builder newRequest(URI uri, String contentType, String entity);

    /**
     * Constructs a new Request with the specified URI, contentType, and entity. Sets the accept property to a default of
     * "&#42;/&#42;", and the content charset property to "UTF-8". This should only be used for sending textual content
     * types, typically via the POST or PUT HTTP methods.
     *
     * @param uri The endpoint URI for this request
     * @param contentType A textual IANA media type
     * @param entity A string entity to send as this request's message body
     * @return The new request object
     */
    Request.Builder newRequest(String uri, String contentType, String entity);
}
