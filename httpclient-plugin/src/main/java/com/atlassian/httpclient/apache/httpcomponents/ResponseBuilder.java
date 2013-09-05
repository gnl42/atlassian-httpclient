package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.Response.Builder;

import static com.atlassian.fugue.Option.some;

public class ResponseBuilder implements Response.Builder
{

    static Response.Builder builder()
    {
        return new ResponseBuilder();
    }

    private Option<Entity> entity = Option.none();
    private final Headers.Builder headers = HeadersBuilder.builder();
    private int statusCode;
    private String statusText;

    private ResponseBuilder() {}

    @Override
    public Builder setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public Builder setStatusText(String statusText)
    {
        this.statusText = statusText;
        return this;
    }

    @Override
    public Builder setHeaders(Headers headers)
    {
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public Builder setEntity(Entity entity)
    {
        this.entity = some(entity);
        return this;
    }

    @Override
    public Response build()
    {
        return new DefaultResponse(entity, headers.build(), statusCode, statusText);
    }
}
