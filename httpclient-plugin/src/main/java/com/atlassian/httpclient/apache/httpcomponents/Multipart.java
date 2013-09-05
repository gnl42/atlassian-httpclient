package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.util.concurrent.Supplier;
import com.atlassian.util.concurrent.Suppliers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

class Multipart implements Entity
{
    private final Headers headers;
    private final Supplier<InputStream> inputStream;
    private final Supplier<String> body;

    public Multipart(Headers headers, InputStream inputStream)
    {
        this.headers = headers;
        this.inputStream = Suppliers.memoize(inputStream);
        body = LazyBody.body(inputStream, headers, Integer.MAX_VALUE);
    }

    public Multipart(Headers headers, final String body)
    {
        this.headers = headers;
        this.inputStream = new Supplier<InputStream>()
        {
            @Override
            public InputStream get()
            {
                return new ByteArrayInputStream(body.getBytes());
            }
        };
        this.body = Suppliers.memoize(body);
    }

    @Override
    public Headers headers()
    {
        return headers;
    }

    @Override
    public InputStream inputStream()
    {
        return inputStream.get();
    }

    @Override
    public String asString()
    {
        return body.get();
    }
}
