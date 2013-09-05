package com.atlassian.httpclient.api;

import java.io.InputStream;

public interface Entity
{
    Headers headers();

    String asString();

    InputStream inputStream();

    interface Builder extends Buildable<Entity>
    {

        /**
         * Sets this object's entity as an input stream. Invocations of this method
         * reset this object's <code>hasReadEntity()</code> state to
         * <code>false</code>. It is recommended to also set this object's content
         * charset property when setting an entity stream for a textual media type
         * (or using the overloaded form that takes both the entity stream and
         * charset in the same call). Clients of this object should assume the HTTP
         * standard of <code>ISO-8859-1 (latin-1)</code> for the content charset
         * property if a textual media type is set but no explcit charset was
         * provided for this message. A charset should NOT be provided for entity
         * streams targetting binary media types.
         *
         * @param entityStream An entity input stream ready to be read
         * @return This object, for builder-style chaining
         */
        Builder setStream(InputStream entityStream);

        /**
         * Sets this object's entity as an input stream, encoded with the specified
         * charset. Invocations of this method reset this object's
         * <code>hasReadEntity()</code> state to <code>false</code>. This method
         * should only be called for entity streams targetting textual media types
         * -- that is, it's nonsensical to set the charset of an entity stream for
         * binary media types (e.g. image/*, etc).
         *
         * @param entityStream An entity input stream ready to be read
         * @param charset The charset in which the entity stream is encoded
         * @return This object, for builder-style chaining
         */
        Builder setStream(InputStream entityStream, String charset);

        /**
         * Sets this object's entity stream from a string. Using this method of
         * setting the entity automatically sets this object's content charset
         * property to "UTF-8" if the entity is not null.
         *
         * @param entity An entity string
         * @return This object, for builder-style chaining
         */
        Builder setString(String entity);

        Builder setMaxEntitySize(long maxEntitySize);
    }
}
