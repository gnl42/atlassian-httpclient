package com.atlassian.httpclient.api;

public final class ResponseTransformationException extends RuntimeException
{
    public ResponseTransformationException(Throwable throwable)
    {
        super(throwable);
    }
}
