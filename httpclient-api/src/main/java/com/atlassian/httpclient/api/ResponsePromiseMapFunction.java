package com.atlassian.httpclient.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

final class ResponsePromiseMapFunction<O> implements Function<Response, O>
{
    private final Map<StatusRange, Function<Response, ? extends O>> functions = newHashMap();
    private Function<Response, ? extends O> othersFunction;

    private volatile boolean matched;

    ResponsePromiseMapFunction()
    {
    }

    public void addStatusRangeFunction(StatusRange range, Function<Response, ? extends O> func)
    {
        functions.put(range, func);
    }

    public void setOthersFunction(Function<Response, ? extends O> othersFunction)
    {
        this.othersFunction = othersFunction;
    }

    public boolean isMatched()
    {
        return matched;
    }

    @Override
    public O apply(Response response)
    {
        if (matched)
        {
            throw new IllegalStateException("Already matched");
        }

        final int statusCode = response.getStatusCode();
        final Map<StatusRange, Function<Response, ? extends O>> matchingFunctions = Maps.filterKeys(functions, new Predicate<StatusRange>()
        {
            @Override
            public boolean apply(StatusRange input)
            {
                return input.isIn(statusCode);
            }
        });

        if (matchingFunctions.isEmpty())
        {
            if (othersFunction != null)
            {
                matched = true;
                return othersFunction.apply(response);
            }
            throw new IllegalStateException("Could not match any function to status " + statusCode);
        }

        if (matchingFunctions.size() > 1)
        {
            throw new IllegalStateException("Found multiple functions for status " + statusCode);
        }

        matched = true;

        // when there we found one and only one function!
        return Iterables.getLast(matchingFunctions.values()).apply(response);
    }

    static interface StatusRange
    {
        boolean isIn(int code);
    }
}
