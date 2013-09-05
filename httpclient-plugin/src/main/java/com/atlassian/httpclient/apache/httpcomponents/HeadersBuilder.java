package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Pair;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Headers.Builder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;

class HeadersBuilder implements Headers.Builder
{
    public static Builder builder()
    {
        return new HeadersBuilder();
    }

    private ImmutableMap.Builder<String, String> headers = ImmutableMap.builder();

    private HeadersBuilder() {}

    @Override
    public Headers build()
    {
        return new DefaultHeaders(headers.build());
    }

    @Override
    public Builder add(String name, String value)
    {
        headers.put(name, value);
        return this;
    }

    @Override
    public Builder addAll(Iterable<Pair<String, String>> hs)
    {
        for (Pair<String, String> p : hs)
        {
            add(p.left(), p.right());
        }
        return this;
    }

    @Override
    public Builder setContentType(String contentType)
    {
        add(Headers.Name.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public Builder setAccept(String accept)
    {
        add(Headers.Name.ACCEPT, accept);
        return this;
    }

    // TODO work out what to do with charset stuff
    private String parseCharset(String value)
    {
        String contentType = "";
        if (value != null)
        {
            String[] parts = value.split(";");
            if (parts.length >= 1)
            {
                contentType = parts[0].trim();
            }
            if (parts.length >= 2)
            {
                String subtype = parts[1].trim();
                if (subtype.startsWith("charset="))
                {
                    setContentCharset(subtype.substring(8));
                }
                else if (subtype.startsWith("boundary="))
                {
                    contentType = contentType.concat(';' + subtype);
                }
            }
        }
        else
        {
            contentType = "";
        }
        return contentType;
    }

    public Builder setContentCharset(String contentCharset)
    {
        // TODO parse charset and build content-type header appropriately
        Preconditions.checkNotNull(contentCharset);
        contentCharset = Charset.forName(contentCharset).name();
        return this;
    }
}
