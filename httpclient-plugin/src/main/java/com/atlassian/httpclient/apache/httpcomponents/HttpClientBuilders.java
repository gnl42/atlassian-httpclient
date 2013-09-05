package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Builders;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponseTransformation;

class HttpClientBuilders implements Builders
{
    @Override
    public Request.Builder request()
    {
        return RequestBuilder.builder();
    }

    @Override
    public Headers.Builder headers()
    {
        return HeadersBuilder.builder();
    }

    @Override
    public <A> ResponseTransformation.Builder<A> transform()
    {
        return ResponseTransformation.Builder.builder();
    }
}
