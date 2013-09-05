package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Entity.Builder;
import com.atlassian.httpclient.api.Headers;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Builder for HttpEntities with multipart/form data.
 */
class MultipartBuilder implements Entity.Builder
{

    private final MultipartEntity apacheMultipartEntity;

    private MultipartBuilder(final MultipartEntity multipartEntity)
    {
        this.apacheMultipartEntity = multipartEntity;
    }

    @Override
    public Builder setStream(InputStream entityStream)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Builder setStream(InputStream entityStream, String charset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Builder setString(String entity)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Builder setMaxEntitySize(long maxEntitySize)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity build()
    {
        try
        {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            apacheMultipartEntity.writeTo(outputStream);
            final Headers headers = DefaultHeaders.from(apacheMultipartEntity.getContentType());
            return new Multipart(headers, new ByteArrayInputStream(outputStream.toByteArray()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
