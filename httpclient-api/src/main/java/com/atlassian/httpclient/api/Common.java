package com.atlassian.httpclient.api;

import java.io.InputStream;
import java.util.Map;

public interface Common<B extends Common<B>>
{

    /**
     * Sets an HTTP header on this object.  If the header's name is "Content-Type", the value
     * will be parsed into this object's content type and content charset properties, as
     * appropriate.
     *
     * @param name The name of the header to be set
     * @param value The value of the header to be set
     * @return This object, for builder-style chaining
     */
    B setHeader(String name, String value);

    /**
     * Copies the specified map of HTTP headers into this object.  It will also parse any included
     * Content-Type header into its constituent parts of IANA media type and content charset, updating
     * those properties as appropriate.
     *
     * @param headers A map of HTTP headers
     * @return This object, for builder-style chaining
     */
    B setHeaders(Map<String, String> headers);

    /**
     * Sets this object's entity stream from a string.  Using this method of setting the entity
     * automatically sets this object's content charset property to "UTF-8" if the entity is not null.
     *
     * @param entity An entity string
     * @return This object, for builder-style chaining
     */
    B setEntity(String entity);

    /**
     * Sets this object's entity as an input stream.  Invocations of this method reset this object's
     * <code>hasReadEntity()</code> state to <code>false</code>.  It is recommended to also set this
     * object's content charset property when setting an entity stream for a textual media type (or
     * using the overloaded form that takes both the entity stream and charset in the same call).
     * Clients of this object should assume the HTTP standard of <code>ISO-8859-1 (latin-1)</code>
     * for the content charset property if a textual media type is set but no explcit charset was
     * provided for this message.  A charset should NOT be provided for entity streams targetting
     * binary media types.
     *
     * @param entityStream An entity input stream ready to be read
     * @return This object, for builder-style chaining
     */
    B setEntityStream(InputStream entityStream);

    /**
     * Sets the charset for this object's entity, if any.  This value is ignored during headeer access
     * if no entity is present or if the content type property is not set.
     *
     * @param contentCharset The entity's charset value, or null
     * @return This object, for builder-style chaining
     */
    B setContentCharset(String contentCharset);

    /**
     * Sets the IANA media type, for the current entity, if any.  If the <code>contentType</code> argument
     * also contains charset information, this method will have the side effect of parsing the charset
     * out and storing the component parts independently.  The method <code>getContentCharset()</code> can
     * be used to retrieve extracted content charset, if present, and <code>getHeader("Content-Type")</code>
     * can be used to retrieve the entire Content-Type header, complete with charset information, if set.
     * The content type property is required when an entity is present.
     *
     * @param contentType An IANA media type with optional charset information
     * @return This object, for builder-style chaining
     */
    B setContentType(String contentType);

    /**
     * Sets this object's entity as an input stream, encoded with the specified charset.  Invocations of
     * this method reset this object's <code>hasReadEntity()</code> state to <code>false</code>.  This
     * method should only be called for entity streams targetting textual media types -- that is, it's
     * nonsensical to set the charset of an entity stream for binary media types (e.g. image/*, etc).
     *
     * @param entityStream An entity input stream ready to be read
     * @param charset The charset in which the entity stream is encoded
     * @return This object, for builder-style chaining
     */
    B setEntityStream(InputStream entityStream, String charset);
}
