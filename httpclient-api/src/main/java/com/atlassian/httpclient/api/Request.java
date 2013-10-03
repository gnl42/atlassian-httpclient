package com.atlassian.httpclient.api;

import java.net.URI;
import java.util.Map;

/**
 * An interface for building and executing HTTP requests.
 */
public interface Request extends Message
{
    public enum Method { GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE }

    /**
     * Returns this request's URI, if set.
     *
     * @return The URI or null if not yet set
     */
    URI getUri();

    /**
     * Returns this request's Accept header, if set.
     *
     * @return The accept header value
     */
    String getAccept();

    /**
     * Gets an attribute from the request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @param name The attribute name
     * @return The attribute value, or null if not set
     */
    String getAttribute(String name);

    /**
     * Gets all attributes for this request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @return All attributes
     */
    Map<String, String> getAttributes();

    boolean isCacheDisabled();

    Method getMethod();

    interface Builder<T extends Request> extends Common<Builder<T>>, Buildable<T>
    {
        /**
         * Sets this request's URI.  Must not be null by the time the request is executed.
         *
         * @param uri The URI
         * @return This object, for builder-style chaining
         */
        Builder<T> setUri(URI uri);

        /**
         * Sets the Accept header for the request.
         *
         * @param accept An accept header expression containing media types, ranges, and/or quality factors
         * @return This object, for builder-style chaining
         */
        Builder<T> setAccept(String accept);

        /**
         * Bypasses the cache for this request
         *
         * @return This object, for builder-style chaining
         */
        Builder<T> setCacheDisabled();

        /**
         * Sets an attribute on the request.  Attributes are request metadata that are forwarded to the
         * analytics plugin when enabled.
         *
         * @param name The attribute name
         * @param value The attribute value
         * @return This object, for builder-style chaining
         */
        Builder<T> setAttribute(String name, String value);

        /**
         * Sets attributes on the request.  Attributes are request metadata that are forwarded to the
         * analytics plugin when enabled.
         *
         * @param properties A map of attributes
         * @return This object, for builder-style chaining
         */
        Builder<T> setAttributes(Map<String, String> properties);

        /**
         * Sets the entity and any associated headers from an entity builder.
         *
         * @param entityBuilder An entity builder
         * @return This object, for builder-style chaining
         */
        Builder<T> setEntity(EntityBuilder entityBuilder);

        Builder<T> setHeader(String name, String value);

        /**
         * Executes this request through the {@link HttpClient} service as a <code>GET</code> operation.
         * The request SHOULD NOT contain an entity for the <code>GET</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise get();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>POST</code> operation.
         * The request SHOULD contain an entity for the <code>POST</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise post();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>PUT</code> operation.
         * The request SHOULD contain an entity for the <code>PUT</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise put();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>DELETE</code> operation.
         * The request SHOULD NOT contain an entity for the <code>DELETE</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise delete();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>OPTIONS</code> operation.
         * The request MAY contain an entity for the <code>OPTIONS</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise options();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>HEAD</code> operation.
         * The request SHOULD NOT contain an entity for the <code>HEAD</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise head();

        /**
         * Executes this request through the {@link HttpClient} service as a <code>TRACE</code> operation.
         * The request SHOULD contain an entity for the <code>TRACE</code> operation.
         *
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise trace();

        /**
         * Executes this request through the {@link HttpClient} service using the given HTTP method.
         *
         * @param method the HTTP method to use.
         * @return A promise object that can be used to receive the response and handle exceptions
         */
        ResponsePromise execute(Method method);
    }

}
