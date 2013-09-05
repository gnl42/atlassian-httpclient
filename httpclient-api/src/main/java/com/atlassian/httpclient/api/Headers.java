package com.atlassian.httpclient.api;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.util.concurrent.NotNull;

import java.nio.charset.Charset;

public interface Headers extends Iterable<Pair<String, String>>
{

    static final class Name
    {
        public final static String CONTENT_TYPE = "Content-Type";
        public final static String ACCEPT = "Accept";
    }

    Option<String> get(String name);

    /**
     * Returns the IANA media type, minus charset information, for the current entity, if any. To access charset
     * information, use <code>getContentCharset()</code>. To get the full Content-Type header value including charset if
     * specified, use {@link #get(String) get("Content-Type")}.
     *
     * @return An IANA media type
     */
    @NotNull
    String contentType();

    /**
     * Returns the currently set content charset value, if any.
     */
    @NotNull
    Option<Charset> contentCharset();

    Option<Integer> contentLength();

    interface Builder extends Buildable<Headers>
    {
        Builder add(String name, String value);

        Builder addAll(Iterable<Pair<String, String>> headers);

        /**
         * Sets the IANA media type, for the current entity, if any. If the <code>contentType</code> argument also
         * contains charset information, this method will have the side effect of parsing the charset out and storing
         * the component parts independently. The method <code>getContentCharset()</code> can be used to retrieve
         * extracted content charset, if present, and <code>getHeader("Content-Type")</code> can be used to retrieve the
         * entire Content-Type header, complete with charset information, if set. The content type property is required
         * when an entity is present.
         *
         * @param contentType An IANA media type with optional charset information
         */
        Builder setContentType(String contentType);

        /**
         * Sets the charset for this object's entity, if any. This value is ignored during header access if no entity is
         * present or if the content type property is not set.
         *
         * @param contentCharset The entity's charset value, or null
         */
        Builder setContentCharset(String contentCharset);

        /**
         * Sets the Accept for this object's entity, if any.
         */
        Builder setAccept(String accept);
    }
}
