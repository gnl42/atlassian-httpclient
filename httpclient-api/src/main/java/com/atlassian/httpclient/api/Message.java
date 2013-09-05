package com.atlassian.httpclient.api;

import com.atlassian.fugue.Option;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for header and entity management.
 */
public interface Message
{

    /**
     * Returns the current entity if available
     */
    Option<Entity> entity() throws IllegalStateException, IllegalArgumentException;

    /**
     * Returns a map of all headers that have been set on this object. If the content type property has been set, a full
     * "Content-Type" header including content charset, if set, is generated as part of this map.
     *
     * @return The headers map
     */
    Headers headers();

    interface Builder extends Common<Builder>, Buildable<Message>
    {
    }
}
