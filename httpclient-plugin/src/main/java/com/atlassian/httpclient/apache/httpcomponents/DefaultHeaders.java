package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.httpclient.api.Headers;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.apache.http.Header;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

class DefaultHeaders implements Headers
{
    static Headers empty()
    {
        return new DefaultHeaders(ImmutableMap.<String, String>of(), Option.<Charset>none());
    }

    static Headers from(Header... headers)
    {
        return HeadersBuilder.builder().addAll(transform(asList(headers), new Function<Header, Pair<String, String>>()
        {
            @Override
            public Pair<String, String> apply(Header h)
            {
                return pair(h.getName(), h.getValue());
            }
        })).build();
    }

    private final Map<String, String> map;
    private final Option<Charset> charset;

    DefaultHeaders(Map<String, String> map, Option<Charset> charset)
    {
        this.charset = charset;
        this.map = checkNotNull(map);
    }

    @Override
    public Iterator<Pair<String, String>> iterator()
    {
        return Util.pairIterator(map.entrySet());
    }

    @Override
    public Option<String> get(String name)
    {
        return option(map.get(name));
    }

    @Override
    public String contentType()
    {
        return get("Content-Type").getOrElse("text/plain");
    }

    @Override
    public Option<Charset> contentCharset()
    {
        return charset;
    }

    @Override
    public Option<Integer> contentLength()
    {
        return get("Content-Length").flatMap(new Function<String, Option<Integer>>()
        {
            @Override
            public Option<Integer> apply(String input)
            {
                return some(Integer.parseInt(input));
            }
        });
    }
}
