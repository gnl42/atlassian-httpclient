package com.atlassian.httpclient.api;

import com.atlassian.httpclient.api.Response;
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

    @Override
    public O apply(Response response)
    {
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
                return othersFunction.apply(response);
            }
            else
            {
                throw new IllegalStateException("Could not match any function to status " + statusCode);
            }
        }
        else if (matchingFunctions.size() > 1)
        {
            throw new IllegalStateException("Found multiple functions for status " + statusCode);
        }
        else
        {
            // when there we found one and only one function!
            return Iterables.getLast(matchingFunctions.values()).apply(response);
        }
    }

    static interface StatusRange
    {
        boolean isIn(int code);
    }
}
