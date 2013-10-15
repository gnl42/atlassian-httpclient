package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.httpclient.api.ResponseTransformationException;
import com.atlassian.httpclient.api.UnexpectedResponseException;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import javax.annotation.Nullable;

import static com.atlassian.httpclient.apache.httpcomponents.ResponsePromiseMapFunction.StatusRange;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultResponseTransformation<T> implements ResponseTransformation<T>
{
    private final ResponsePromiseMapFunction<T> mapFunctions;
    private final Function<Throwable, ? extends T> failFunction;

    private DefaultResponseTransformation(ResponsePromiseMapFunction<T> mapFunctions, Function<Throwable, ? extends T> failFunction)
    {
        this.mapFunctions = mapFunctions;
        this.failFunction = failFunction;
    }

    @Override
    public Function<Throwable, ? extends T> getFailFunction()
    {
        return failFunction;
    }

    @Override
    public Function<Response, T> getSuccessFunctions()
    {
        return mapFunctions;
    }

    @Override
    public Promise<T> apply(final ResponsePromise responsePromise)
    {
        return responsePromise.transform(this);
    }

    public static <T> Builder<T> builder()
    {
        return new DefaultResponseTransformationBuilder<T>();
    }

    private static class DefaultResponseTransformationBuilder<T> implements Builder<T>
    {
        private final ResponsePromiseMapFunction<T> mapFunctions = new ResponsePromiseMapFunction<T>();
        private Function<Throwable, ? extends T> failFunction = defaultThrowableHandler();

        @Override
        public Builder<T> on(final HttpStatus status, final Function<Response, ? extends T> f)
        {
            return addSingle(status, f);
        }

        @Override
        public Builder<T> on(int statusCode, Function<Response, ? extends T> f)
        {
            return addSingle(statusCode, f);
        }

        @Override
        public Builder<T> informational(Function<Response, ? extends T> f)
        {
            return addRange(HttpStatus.CONTINUE, f);
        }

        // 2xx
        @Override
        public Builder<T> successful(Function<Response, ? extends T> f)
        {
            return addRange(HttpStatus.OK, f);
        }

        @Override
        public Builder<T> ok(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.OK, f);
        }

        @Override
        public Builder<T> created(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.CREATED, f);
        }

        @Override
        public Builder<T> noContent(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.NO_CONTENT, f);
        }

        // 3xx
        @Override
        public Builder<T> redirection(Function<Response, ? extends T> f)
        {
            return addRange(HttpStatus.MULTIPLE_CHOICES, f);
        }

        @Override
        public Builder<T> seeOther(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.SEE_OTHER, f);
        }

        @Override
        public Builder<T> notModified(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.NOT_MODIFIED, f);
        }

        // 4xx
        @Override
        public Builder<T> clientError(Function<Response, ? extends T> f)
        {
            return addRange(HttpStatus.BAD_REQUEST, f);
        }

        @Override
        public Builder<T> badRequest(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.BAD_REQUEST, f);
        }

        @Override
        public Builder<T> unauthorized(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.UNAUTHORIZED, f);
        }

        @Override
        public Builder<T> forbidden(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.FORBIDDEN, f);
        }

        @Override
        public Builder<T> notFound(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.NOT_FOUND, f);
        }

        @Override
        public Builder<T> conflict(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.CONFLICT, f);
        }

        // 5xx
        @Override
        public Builder<T> serverError(Function<Response, ? extends T> f)
        {
            return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
        }

        @Override
        public Builder<T> internalServerError(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
        }

        @Override
        public Builder<T> serviceUnavailable(Function<Response, ? extends T> f)
        {
            return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
        }

        // 4xx and 5xx
        @Override
        public Builder<T> error(Function<Response, ? extends T> f)
        {
            mapFunctions.addStatusRangeFunction(
                    new OrStatusRange(new HundredsStatusRange(HttpStatus.BAD_REQUEST),
                            new HundredsStatusRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);

            return this;
        }

        // 1xx, 3xx, 4xx and 5xx
        @Override
        public Builder<T> notSuccessful(Function<Response, ? extends T> f)
        {
            mapFunctions.addStatusRangeFunction(new NotInStatusRange(new HundredsStatusRange(HttpStatus.OK)), f);
            return this;
        }

        @Override
        public Builder<T> others(Function<Response, ? extends T> f)
        {
            mapFunctions.setOthersFunction(f);
            return this;
        }

        @Override
        public Builder<T> otherwise(final Function<Throwable, T> callback)
        {
            others(new Function<Response, T>()
            {
                @Override
                public T apply(@Nullable Response input)
                {
                    return callback.apply(new UnexpectedResponseException(input));
                }
            });
            fail(callback);
            return this;
        }

        @Override
        public Builder<T> done(final Function<Response, T> f)
        {
            others(new Function<Response, T>()
            {
                @Override
                public T apply(@Nullable Response input)
                {
                    return f.apply(input);
                }
            });
            return this;
        }

        @Override
        public Builder<T> fail(Function<Throwable, ? extends T> f)
        {
            this.failFunction = f;
            return this;
        }

        private DefaultResponseTransformationBuilder<T> addSingle(HttpStatus status, Function<Response, ? extends T> f)
        {
            return addSingle(status.code, f);
        }

        private DefaultResponseTransformationBuilder<T> addSingle(int statusCode, Function<Response, ? extends T> f)
        {
            mapFunctions.addStatusRangeFunction(new SingleStatusRange(statusCode), f);
            return this;
        }

        private DefaultResponseTransformationBuilder<T> addRange(HttpStatus status, Function<Response, ? extends T> f)
        {
            mapFunctions.addStatusRangeFunction(new HundredsStatusRange(status), f);
            return this;
        }


        private Function<Throwable, ? extends T> defaultThrowableHandler()
        {
            return new Function<Throwable, T>()
            {
                @Override
                public T apply(Throwable throwable)
                {
                    if (throwable instanceof RuntimeException)
                    {
                        throw (RuntimeException) throwable;
                    }
                    throw new ResponseTransformationException(throwable);
                }
            };
        }

        @Override
        public ResponseTransformation<T> build()
        {
            return new DefaultResponseTransformation<T>(mapFunctions, failFunction);
        }
    }

    static final class SingleStatusRange implements StatusRange
    {
        private final int statusCode;

        SingleStatusRange(int statusCode)
        {
            this.statusCode = checkNotNull(statusCode);
        }

        @Override
        public boolean isIn(int code)
        {
            return this.statusCode == code;
        }
    }

    static final class HundredsStatusRange implements StatusRange
    {
        private final HttpStatus status;

        private HundredsStatusRange(HttpStatus status)
        {
            this.status = checkNotNull(status);
        }

        @Override
        public boolean isIn(int code)
        {
            final int diff = code - status.code;
            return 0 <= diff && diff < 100;
        }
    }

    static final class NotInStatusRange implements StatusRange
    {
        private final StatusRange range;

        private NotInStatusRange(StatusRange range)
        {
            this.range = checkNotNull(range);
        }

        @Override
        public boolean isIn(int code)
        {
            return !range.isIn(code);
        }
    }

    static final class OrStatusRange implements StatusRange
    {
        private final StatusRange one;
        private final StatusRange two;

        private OrStatusRange(StatusRange one, StatusRange two)
        {
            this.one = checkNotNull(one);
            this.two = checkNotNull(two);
        }

        @Override
        public boolean isIn(int code)
        {
            return one.isIn(code) || two.isIn(code);
        }
    }
}
