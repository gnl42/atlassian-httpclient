package com.atlassian.httpclient.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;

final class ResponsePromiseMapFunction<O> implements Function<Response, O> {
    private final ImmutableMap<StatusRange, Function<Response, ? extends O>> functions;
    private Function<Response, ? extends O> othersFunction;

    private ResponsePromiseMapFunction(ImmutableMap<StatusRange, Function<Response, ? extends O>> functions,
                                       Function<Response, ? extends O> othersFunction) {
        this.functions = functions;
        this.othersFunction = othersFunction;
    }

    public static <T> ResponsePromiseMapFunctionBuilder<T> builder() {
        return new ResponsePromiseMapFunctionBuilder<T>();
    }

    public static final class ResponsePromiseMapFunctionBuilder<T> implements Buildable<ResponsePromiseMapFunction<T>> {
        private Map<StatusRange, Function<Response, ? extends T>> functionMap = Maps.newHashMap();
        private Function<Response, ? extends T> othersFunction;

        public void addStatusRangeFunction(StatusRange range, Function<Response, ? extends T> func) {
            functionMap.put(range, func);
        }

        public void setOthersFunction(Function<Response, ? extends T> othersFunction) {
            this.othersFunction = othersFunction;
        }

        @Override
        public ResponsePromiseMapFunction<T> build() {
            return new ResponsePromiseMapFunction<T>(ImmutableMap.copyOf(functionMap), othersFunction);
        }
    }

    @Override
    public O apply(Response response) {
        final int statusCode = response.getStatusCode();
        final Map<StatusRange, Function<Response, ? extends O>> matchingFunctions = Maps.filterKeys(functions, new Predicate<StatusRange>() {
            @Override
            public boolean apply(StatusRange input) {
                return input.isIn(statusCode);
            }
        });

        if (matchingFunctions.isEmpty()) {
            if (othersFunction != null) {
                return othersFunction.apply(response);
            } else {
                throw new IllegalStateException("Could not match any function to status " + statusCode);
            }
        } else if (matchingFunctions.size() > 1) {
            throw new IllegalStateException("Found multiple functions for status " + statusCode);
        } else {
            // when there we found one and only one function!
            return Iterables.getLast(matchingFunctions.values()).apply(response);
        }
    }

    static interface StatusRange {
        boolean isIn(int code);
    }
}
