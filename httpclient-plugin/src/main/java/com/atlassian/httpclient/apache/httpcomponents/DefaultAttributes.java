package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.httpclient.api.Attributes;

import java.util.Iterator;
import java.util.Map;

class DefaultAttributes implements Attributes
{
    private final Map<String, String> map;

    DefaultAttributes(Map<String, String> map)
    {
        this.map = map;
    }

    @Override
    public Option<String> get(String name)
    {
        return Option.option(map.get(name));
    }

    @Override
    public Iterator<Pair<String, String>> iterator()
    {
        return Util.pairIterator(map.entrySet());
    }
}
