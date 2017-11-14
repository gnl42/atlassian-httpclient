package com.atlassian.httpclient.api;

import com.atlassian.fugue.Option;

import java.io.InputStream;
import java.util.Map;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for
 * header and entity management.
 */
public interface Message {
    /**
     * Returns the IANA media type, minus charset information, for the current entity, if any.
     * To access charset information, use <code>getContentCharset()</code>.  To get the full
     * Content-Type header value including charset if specified, use <code>getHeader("Content-Type")</code>.
     *
     * @return An IANA media type, or null
     */
    String getContentType();

    /**
     * Returns the currently set content charset value, if any.
     *
     * @return The current content charset
     */
    String getContentCharset();

    /**
     * Returns the current entity as an input stream, or null if not set.  Use <code>hasEntity()</code>
     * to check if this message has an entity value.
     *
     * @return An input stream for the current entity, or null if not set
     * @throws IllegalStateException If the non-null entity has already been accessed once, through
     *                               any accessor for this object
     */
    InputStream getEntityStream() throws IllegalStateException;

    /**
     * Returns the current entity in <code>String</code> form, if available, converting the underlying
     * entity stream to a string using the currently set content charset, or defaulting to the HTTP
     * standard of "ISO-8859-1" if no content charset has been specified.
     *
     * @return The entity string, or null if no entity has been set
     * @throws IllegalStateException    If the non-null entity has already been accessed once, through
     *                                  any accessor for this object.  Also thrown if underlying body cannot be converted into a String
     * @throws IllegalArgumentException If the entity exceeds the maximum size
     */
    String getEntity() throws IllegalStateException, IllegalArgumentException;

    /**
     * Returns whether or not an entity has been set on this object.  Use this instead of calling
     * an entity getter to test for presence of an entity, as the getters will affect this object's
     * <code>hasReadEntity()</code> state.
     *
     * @return This object, for builder-style chaining
     */
    boolean hasEntity();

    /**
     * Returns whether or not the current entity property, if any, has been read from this object.
     * If this method returns true, any further calls to entity property accessors on this object
     * will result in an {@link IllegalStateException} being thrown.
     *
     * @return True if the entity has already been read
     */
    boolean hasReadEntity();

    /**
     * Returns a map of all headers that have been set on this object.  If the content type property
     * has been set, a full "Content-Type" header including content charset, if set, is generated as
     * part of this map.
     *
     * @return The headers map
     */
    Map<String, String> getHeaders();

    /**
     * Returns the specified header by name.  If "Content-Type" is requested, the value will be
     * constructed from this object's content type and content charset properties, if set and
     * as appropriate.
     *
     * @param name The name of the header to fetch
     * @return The value of the named header, or null if not set
     */
    String getHeader(String name);

    /**
     * Returns the content length for the entity in the message. For requests, this would be the content length of the
     * entity that you set, or the content length that you can set manually (e.g. for streamed entities). For responses,
     * this would be the content length from the header.
     *
     * @return The content length as an option. None is returned if no content length has been set or not present.
     */
    Option<Long> getContentLength();
}
