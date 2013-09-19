package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Headers.Builder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Map;

class HeadersBuilder implements Headers.Builder
{
    private Charset contentCharset;

    public static Builder builder()
    {
        return new HeadersBuilder();
    }

    private ImmutableMap.Builder<String, String> headers = ImmutableMap.builder();

    private HeadersBuilder() {}

    @Override
    public Headers build()
    {
        return new DefaultHeaders(headers.build(), Option.option(contentCharset));
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
    public Builder addAll(final Map<String, String> headers)
    {
        headers.putAll(headers);
        return this;
    }

    @Override
    public Builder setContentType(String contentType)
    {
        add(Headers.Name.CONTENT_TYPE, contentType);
        parseCharset(contentType).foreach(new Effect<String>()
        {
            @Override
            public void apply(final String contentCharset)
            {
                setContentCharset(contentCharset);
            }
        });
        return this;
    }

    @Override
    public Builder setAccept(String accept)
    {
        add(Headers.Name.ACCEPT, accept);
        return this;
    }

    private Option<String> parseCharset(String contentType)
    {
        if (contentType != null)
        {
            String[] parts = contentType.split(";");
            if (parts.length >= 2)
            {
                String subtype = parts[1].trim();
                if (subtype.startsWith("charset="))
                {
                    return Option.some(subtype.substring(8));
                }
            }
        }
        return Option.none();
    }

    public Builder setContentCharset(String contentCharset)
    {
        Preconditions.checkNotNull(contentCharset);
        this.contentCharset = Charset.forName(contentCharset);
        return this;
    }
}
