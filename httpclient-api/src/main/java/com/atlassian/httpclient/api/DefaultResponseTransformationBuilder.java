package com.atlassian.httpclient.api;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.ResponseTransformation.Builder;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

final class DefaultResponseTransformationBuilder<O> extends Builder<O>
{
    private final Iterable<Function<Response, Option<O>>> stack;
    private final Function<Throwable, ? extends O> failFunction;

    DefaultResponseTransformationBuilder(DefaultResponseTransformationBuilder<O> that, Function<Response, Option<O>> partial)
    {
        this(copyOf(concat(that.stack, singleton(partial))), that.failFunction);
    }

    DefaultResponseTransformationBuilder(Iterable<Function<Response, Option<O>>> stack, Function<Throwable, ? extends O> failFunction)
    {
        this.stack = checkNotNull(stack);
        this.failFunction = checkNotNull(failFunction);
    }

    DefaultResponseTransformationBuilder()
    {
        this.stack = emptyList();
        this.failFunction = defaultThrowableHandler();
    }

    @Override
    public Builder<O> on(HttpStatus status, Function<Response, ? extends O> f)
    {
        return addSingle(status, f);
    }

    @Override
    public Builder<O> on(int statusCode, Function<Response, ? extends O> f)
    {
        return addSingle(statusCode, f);
    }

    @Override
    public Builder<O> informational(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.CONTINUE, f);
    }

    // 2xx
    @Override
    public Builder<O> successful(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.OK, f);
    }

    @Override
    public Builder<O> ok(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.OK, f);
    }

    @Override
    public Builder<O> created(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CREATED, f);
    }

    @Override
    public Builder<O> noContent(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NO_CONTENT, f);
    }

    // 3xx
    @Override
    public Builder<O> redirection(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.MULTIPLE_CHOICES, f);
    }

    @Override
    public Builder<O> seeOther(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SEE_OTHER, f);
    }

    @Override
    public Builder<O> notModified(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_MODIFIED, f);
    }

    // 4xx
    @Override
    public Builder<O> clientError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public Builder<O> badRequest(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public Builder<O> unauthorized(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.UNAUTHORIZED, f);
    }

    @Override
    public Builder<O> forbidden(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.FORBIDDEN, f);
    }

    @Override
    public Builder<O> notFound(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_FOUND, f);
    }

    @Override
    public Builder<O> conflict(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CONFLICT, f);
    }

    // 5xx
    @Override
    public Builder<O> serverError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public Builder<O> internalServerError(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public Builder<O> serviceUnavailable(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
    }

    // 4xx and 5xx
    @Override
    public Builder<O> error(Function<Response, ? extends O> f)
    {
        return append(Predicates.<Response>or(matchesRange(HttpStatus.BAD_REQUEST), matchesRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);
    }

    // 1xx, 3xx, 4xx and 5xx
    @Override
    public Builder<O> notSuccessful(Function<Response, ? extends O> f)
    {
        return append(Predicates.not(matchesRange(HttpStatus.OK)), f);
    }

    @Override
    public Builder<O> others(Function<Response, ? extends O> f)
    {
        return append(Predicates.<Response>alwaysTrue(), f);
    }

    @Override
    public Builder<O> otherwise(final Function<Throwable, O> callback)
    {
        return new DefaultResponseTransformationBuilder<O>(this.stack, callback);
    }

    @Override
    public Builder<O> done(final Function<Response, O> f)
    {
        return others(new Function<Response, O>()
        {
            @Override
            public O apply(@Nullable Response input)
            {
                return f.apply(input);
            }
        });
    }

    @Override
    public Builder<O> fail(Function<Throwable, ? extends O> f)
    {
        return new DefaultResponseTransformationBuilder<O>(this.stack, f);
    }

    @Override
    public ResponseTransformation<O> build()
    {
        return new ResponseTransformation<O>()
        {
            private final Function<Response, O> success = PartialFunctions.compose(stack);

            @Override
            public Promise<O> transform(Promise<Response> promise)
            {
                return promise.fold(failFunction, success);
            }
        };
    }

    Builder<O> addSingle(HttpStatus status, Function<Response, ? extends O> f)
    {
        return addSingle(status.code, f);
    }

    Builder<O> addSingle(int statusCode, Function<Response, ? extends O> f)
    {
        return append(matchesStatus(statusCode), f);
    }

    Builder<O> addRange(HttpStatus status, Function<Response, ? extends O> f)
    {
        return append(matchesRange(status), f);
    }

    Builder<O> append(Predicate<Response> p, Function<Response, ? extends O> f)
    {
        return copy(PartialFunctions.from(p, f));
    }

    Builder<O> copy(Function<Response, Option<O>> f)
    {
        return new DefaultResponseTransformationBuilder<O>(this, f);
    }

    Predicate<Response> matchesStatus(final int code)
    {
        return new StatusCodePredicate(new Predicate<Integer>()
        {
            @Override
            public boolean apply(Integer input)
            {
                return code == input;
            }
        });
    }

    Predicate<Response> matchesRange(final HttpStatus status)
    {
        return new StatusCodePredicate(new Predicate<Integer>()
        {
            @Override
            public boolean apply(Integer input)
            {
                final int diff = input - status.code;
                return 0 <= diff && diff < 100;
            }
        });
    }

    private static <A> Function<Throwable, ? extends A> defaultThrowableHandler()
    {
        return new Function<Throwable, A>()
        {
            @Override
            public A apply(Throwable throwable)
            {
                if (throwable instanceof RuntimeException)
                {
                    throw (RuntimeException) throwable;
                }
                if (throwable instanceof Error)
                {
                    throw (Error) throwable;
                }
                throw new ResponseTransformationException(throwable);
            }
        };
    }

    static final class StatusCodePredicate implements Predicate<Response>
    {
        private final Predicate<Integer> p;

        private StatusCodePredicate(Predicate<Integer> p)
        {
            this.p = checkNotNull(p);
        }

        @Override
        public boolean apply(Response input)
        {
            return p.apply(input.statusCode());
        }
    }
}
