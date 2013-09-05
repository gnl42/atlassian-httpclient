package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class HttpEntityFactory
{
    static HttpEntity getHttpEntity(final Entity entity)
    {
        InputStream entityStream = checkNotNull(entity.inputStream());
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
        else
        {
            return new InputStreamEntity(entityStream, -1);
        }
    }
}
