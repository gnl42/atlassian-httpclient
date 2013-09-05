package com.atlassian.httpclient.api;

public interface Builders
{
    Request.Builder request();

    Headers.Builder headers();

    <A> ResponseTransformation.Builder<A> transform();
}
