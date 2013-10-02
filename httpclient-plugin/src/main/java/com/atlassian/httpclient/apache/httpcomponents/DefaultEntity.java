package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;

import java.io.InputStream;

public class DefaultEntity implements Entity
{
    private final int maxEntitySize;
    private final InputStream entityStream;
    private final Headers headers;

    public DefaultEntity(Headers headers, int maxEntitySize, InputStream entityStream)
    {
        this.headers = headers;
        this.maxEntitySize = maxEntitySize;
        this.entityStream = entityStream;
    }

    @Override
    public Headers headers()
    {
        return headers;
    }

    @Override
    public InputStream inputStream()
    {
        Option<Integer> contentLength = headers.contentLength();
        contentLength.foreach(new Effect<Integer>()
        {
            @Override
            public void apply(final Integer contentLength)
            {
                if (contentLength > maxEntitySize)
                {
                    throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
                }
            }
        });
        return entityStream;
    }

    @Override
    public String asString()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
