package com.atlassian.httpclient.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

final class ResponsePromiseMapFunction<O> implements Function<Response, O>
{
    private final Map<StatusRange, Function<Response, ? extends O>> functions;
    private final Function<Response, ? extends O> othersFunction;

    ResponsePromiseMapFunction(Map<StatusRange, Function<Response, ? extends O>> functions, Function<Response, ? extends O> othersFunction)
    {
        this.functions = ImmutableMap.copyOf(functions);
        this.othersFunction = othersFunction;
    }

    public static <T> Function<Response, T> newUnexpectedResponseFunction()
    {
        return new Function<Response, T>()
        {
            @Override
            public T apply(@Nullable Response response)
            {
                throw new UnexpectedResponseException(response);
            }
        };
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
            throw new IllegalStateException("Could not match any function to status " + statusCode);
        }

        if (matchingFunctions.size() > 1)
        {
            throw new IllegalStateException("Found multiple functions for status " + statusCode);
        }

        // when there we found one and only one function!
        return Iterables.getLast(matchingFunctions.values()).apply(response);
    }

    static interface StatusRange
    {
        boolean isIn(int code);
    }
}
