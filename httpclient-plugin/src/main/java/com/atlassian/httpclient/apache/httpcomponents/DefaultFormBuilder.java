package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.FormBuilder;
import com.atlassian.httpclient.api.Headers;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public final class DefaultFormBuilder implements FormBuilder
{
    private Map<String, List<String>> parameters = newLinkedHashMap();

    public FormBuilder addParam(String name)
    {
        return addParam(name, null);
    }

    public FormBuilder addParam(String name, String value)
    {
        List<String> values = parameters.get(name);
        if (values == null)
        {
            values = newLinkedList();
            parameters.put(name, values);
        }
        values.add(value);
        return this;
    }

    public FormBuilder setParam(String name, Iterable<String> values)
    {
        parameters.put(name, newLinkedList(values));
        return this;
    }

    public Entity build()
    {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : parameters.entrySet())
        {
            String name = encode(entry.getKey());
            List<String> values = entry.getValue();
            for (String value : values)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    buf.append("&");
                }
                buf.append(name);
                if (value != null)
                {
                    buf.append("=");
                    buf.append(encode(value));
                }
            }
        }

        final byte[] bytes = buf.toString().getBytes(Charset.forName("UTF-8"));

        return new Entity()
        {
            @Override
            public Headers headers()
            {
                return new DefaultHeaders(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded"), Option.some(Charsets.UTF_8));
            }

            @Override
            public InputStream inputStream()
            {
                return new ByteArrayInputStream(bytes);
            }
        };
    }

    private String encode(String str)
    {
        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return str;
    }
}
