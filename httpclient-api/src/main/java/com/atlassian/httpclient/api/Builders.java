package com.atlassian.httpclient.api;

public interface Builders
{
    Request.Builder request();

    Headers.Builder headers();

    Entity.Builder entity();

    <A> ResponseTransformation.Builder<A> transform();
}
