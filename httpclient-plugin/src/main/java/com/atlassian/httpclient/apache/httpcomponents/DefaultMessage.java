package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Message;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for header and entity management.
 */
abstract class DefaultMessage implements Message
{
    private final Headers headers;
    private final Option<Entity> entity;

    DefaultMessage(Headers headers, Option<Entity> entity)
    {
        this.entity = entity;
        this.headers = headers;
    }

    public Option<Entity> entity()
    {
        return entity;
    }

    public Headers headers()
    {
        return headers;
    }
}
