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
     * Gets all attributes for this request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @return All attributes
     */
    Attributes attributes();

    Method method();

    interface Builder extends Common<Builder>, Buildable<Request>
    {

        /**
         * Sets this request's Method to GET
         */
        Builder get();

        /**
         * Sets this request's Method to POST
         */
        Builder post();

        /**
         * Sets this request's Method to PUT
         */
        Builder put();

        /**
         * Sets this request's Method to DELETE
         */
        Builder delete();

        /**
         * Sets this request's Method to OPTIONS
         */
        Builder options();

        /**
         * Sets this request's Method to HEAD
         */
        Builder head();

        /**
         * Sets this request's Method to TRACE
         */
        Builder trace();

        /**
         * Sets this request's Method
         */
        Builder setMethod(Method method);

        /**
         * Sets this request's URI. Must not be null by the time the request is
         * executed.
         */
        Builder uri(URI uri);

        /**
         * Sets this request's URI. Must not be null by the time the request is
         * executed.
         */
        Builder url(String url);

        /**
         * Sets the Accept header for the request.
         *
         * @param accept An accept header expression containing media types, ranges,
         * and/or quality factors
         */
        Builder setAccept(String accept);

        /**
         * Bypasses the cache for this request
         */
        // TODO is this a request attribute?
        //Builder setCacheDisabled();

        /**
         * Sets an attribute on the request. Attributes are request metadata that
         * are forwarded to the analytics plugin when enabled.
         *
         * @param name The attribute name
         * @param value The attribute value
         * @return This object, for builder-style chaining
         */
        Builder setAttribute(String name, String value);

        /**
         * Sets attributes on the request. Attributes are request metadata that are
         * forwarded to the analytics plugin when enabled.
         *
         * @param properties A map of attributes
         * @return This object, for builder-style chaining
         */
        Builder setAttributes(Map<String, String> properties);
    }
}
