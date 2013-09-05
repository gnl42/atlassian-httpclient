package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Pair;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nullable;

final class Util
{
    static <A, B> Iterator<Pair<A, B>> pairIterator(Iterable<Entry<A, B>> iterable)
    {
        return Iterators.transform(iterable.iterator(), new Function<Entry<A, B>, Pair<A, B>>()
        {
            @Override
            public Pair<A, B> apply(@Nullable Entry<A, B> input)
            {
                return Pair.pair(input.getKey(), input.getValue());
            }
        });
    }
}
