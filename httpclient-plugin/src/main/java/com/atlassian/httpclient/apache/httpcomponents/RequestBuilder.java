package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Request.Builder;
import com.atlassian.httpclient.api.Request.Method;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Map;

public class RequestBuilder implements Request.Builder
{
    static Request.Builder builder()
    {
        return new RequestBuilder();
    }

    private URI uri;
    private Method method;
    private Option<Entity> entity = Option.none();
    private final ImmutableMap.Builder<String, String> attributes = ImmutableMap.builder();
    private final Headers.Builder headers = HeadersBuilder.builder();

    private RequestBuilder() {}

    @Override
    public Request build()
    {
        return new DefaultRequest(headers.build(), entity, method, uri, new DefaultAttributes(attributes.build()));
    }

    @Override
    public Request.Builder setAccept(String accept)
    {
        headers.setAccept(accept);
        return this;
    }

    @Override
    public Request.Builder setAttribute(String name, String value)
    {
        attributes.put(name, value);
        return this;
    }

    @Override
    public Request.Builder setAttributes(Map<String, String> attrs)
    {
        attributes.putAll(attrs);
        return this;
    }

    @Override
    public Request.Builder setEntity(Entity entity)
    {
        this.entity = Option.some(entity);
        return this;
    }

    @Override
    public Request.Builder setHeaders(Headers headers)
    {
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public Builder setMethod(Method method)
    {
        this.method = method;
        return this;
    }

    @Override
    public Builder uri(final URI uri)
    {
        this.uri = uri;
        return this;
    }

    @Override
    public Builder url(final String url)
    {
        return uri(URI.create(url));
    }

    @Override
    public Builder get()
    {
        setMethod(Method.GET);
        return this;
    }

    @Override
    public Builder post()
    {
        setMethod(Method.POST);
        return this;
    }

    @Override
    public Builder put()
    {
        setMethod(Method.PUT);
        return this;
    }

    @Override
    public Builder delete()
    {
        setMethod(Method.DELETE);
        return this;
    }

    @Override
    public Builder options()
    {
        setMethod(Method.OPTIONS);
        return this;
    }

    @Override
    public Builder head()
    {
        setMethod(Method.HEAD);
        return this;
    }

    @Override
    public Builder trace()
    {
        setMethod(Method.TRACE);
        return this;
    }
}
