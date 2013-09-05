package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.util.concurrent.Supplier;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultEntity implements Entity
{

    private final InputStream entityStream;
    private final Supplier<String> body;
    private final Headers headers;

    public DefaultEntity(Headers headers, int maxSize, InputStream entityStream)
    {
        this.headers = headers;
        this.entityStream = entityStream;
        body = LazyBody.body(inputStream(), headers, maxSize);
    }

    @Override
    public Headers headers()
    {
        return headers;
    }

    @Override
    public InputStream inputStream()
    {
        return entityStream;
    }

    public String asString()
    {
        return body.get();
    }

    HttpEntity getHttpEntity()
    {
        if (entityStream instanceof ByteArrayInputStream)
        {
            byte[] bytes;
            if (entityStream instanceof EntityByteArrayInputStream)
            {
                bytes = ((EntityByteArrayInputStream) entityStream).getBytes();
            }
            else
            {
                try
                {
                    bytes = ByteStreams.toByteArray(entityStream);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return new ByteArrayEntity(bytes);
        }
        return new InputStreamEntity(entityStream, -1);
    }
}
