package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Entity.Builder;
import com.atlassian.httpclient.api.FormBuilder;
import com.atlassian.httpclient.api.Headers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

final class DefaultFormBuilder implements FormBuilder
{
    private Map<String, Iterable<String>> parameters = newLinkedHashMap();

    public FormBuilder addParam(String name)
    {
        return addParam(name, null);
    }

    public FormBuilder addParam(String name, String value)
    {
        ImmutableList<String> newValue = ImmutableList.of(value);
        Iterable<String> values = parameters.get(name);
        if (values == null)
        {
            values = newValue;
        }
        else
        {
            values = Iterables.concat(values, newValue);
        }
        parameters.put(name, values);
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
        for (Map.Entry<String, Iterable<String>> entry : parameters.entrySet())
        {
            String name = encode(entry.getKey());
            Iterable<String> values = entry.getValue();
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
                return new DefaultHeaders(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
            }

            @Override
            public InputStream inputStream()
            {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public String asString()
            {
                return new String(bytes, Charset.forName("UTF-8"));
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
}
