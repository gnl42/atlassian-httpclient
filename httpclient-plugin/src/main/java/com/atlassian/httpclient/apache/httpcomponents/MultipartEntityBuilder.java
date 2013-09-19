package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import org.apache.http.Header;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class MultipartEntityBuilder implements Entity.Builder
{
    private MultipartEntity apacheMultipartEntity;

    public static MultipartEntityBuilder builder()
    {
        return new MultipartEntityBuilder();
    }

    private MultipartEntityBuilder() {}

    public MultipartEntityBuilder entity(MultipartEntity entity)
    {
        this.apacheMultipartEntity = entity;
        return this;
    }

    @Override
    public Entity build()
    {
        final Supplier<InputStream> streamSupplier = Suppliers.memoize(new Supplier<InputStream>()
        {
            @Override
            public InputStream get()
            {
                try
                {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    apacheMultipartEntity.writeTo(outputStream);
                    return new ByteArrayInputStream(outputStream.toByteArray());
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
        final Header header = apacheMultipartEntity.getContentType();
        final Map<String, String> headers = Maps.newHashMap();
        headers.put(header.getName(), header.getValue());

        return new Entity()
        {
            @Override
            public Headers headers()
            {
                return new DefaultHeaders(headers, Option.<Charset>none());
            }

            @Override
            public InputStream inputStream()
            {
                return streamSupplier.get();
            }
        };
    }
}
