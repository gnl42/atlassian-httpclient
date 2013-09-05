package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Attributes;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Request;

import java.net.URI;

public class DefaultRequest extends DefaultMessage implements Request
{
    private final Method method;
    private final URI uri;
    private final Attributes attributes;

    DefaultRequest(Headers headers, Option<Entity> entity, Method method, URI uri, Attributes attributes)
    {
        super(headers, entity);
        this.method = method;
        this.uri = uri;
        this.attributes = attributes;
    }

    @Override
    public Method method()
    {
        return method;
    }

    @Override
    public URI uri()
    {
        return uri;
    }

    @Override
    public String getAccept()
    {
        return headers().get("Accept").getOrElse("*/*");
    }

    @Override
    public Attributes attributes()
    {
        return attributes;
    }
}
