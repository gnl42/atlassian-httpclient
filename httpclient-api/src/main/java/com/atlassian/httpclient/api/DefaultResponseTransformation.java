package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static com.atlassian.httpclient.api.ResponsePromiseMapFunction.StatusRange;
import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
final class DefaultResponseTransformation<O> implements ResponseTransformation<O>
{
    private final ResponsePromiseMapFunction<O> mapFunctions = new ResponsePromiseMapFunction<O>();
    private volatile Function<Throwable, ? extends O> failFunction = defaultThrowableHandler();
    private final ResponsePromise responsePromise;

    DefaultResponseTransformation(ResponsePromise baseResponsePromise)
    {
        this.responsePromise = checkNotNull(baseResponsePromise);
    }

    @Override
    public ResponseTransformation<O> on(HttpStatus status, Function<Response, ? extends O> f)
    {
        return addSingle(status, f);
    }

    @Override
    public ResponseTransformation<O> on(int statusCode, Function<Response, ? extends O> f)
    {
        return addSingle(statusCode, f);
    }

    @Override
    public ResponseTransformation<O> informational(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.CONTINUE, f);
    }

    // 2xx
    @Override
    public ResponseTransformation<O> successful(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.OK, f);
    }

    @Override
    public ResponseTransformation<O> ok(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.OK, f);
    }

    @Override
    public ResponseTransformation<O> created(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CREATED, f);
    }

    @Override
    public ResponseTransformation<O> noContent(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NO_CONTENT, f);
    }

    // 3xx
    @Override
    public ResponseTransformation<O> redirection(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.MULTIPLE_CHOICES, f);
    }

    @Override
    public ResponseTransformation<O> seeOther(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SEE_OTHER, f);
    }

    @Override
    public ResponseTransformation<O> notModified(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_MODIFIED, f);
    }

    // 4xx
    @Override
    public ResponseTransformation<O> clientError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public ResponseTransformation<O> badRequest(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public ResponseTransformation<O> unauthorized(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.UNAUTHORIZED, f);
    }

    @Override
    public ResponseTransformation<O> forbidden(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.FORBIDDEN, f);
    }

    @Override
    public ResponseTransformation<O> notFound(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_FOUND, f);
    }

    @Override
    public ResponseTransformation<O> conflict(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CONFLICT, f);
    }

    // 5xx
    @Override
    public ResponseTransformation<O> serverError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public ResponseTransformation<O> internalServerError(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public ResponseTransformation<O> serviceUnavailable(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
    }

    // 4xx and 5xx
    @Override
    public ResponseTransformation<O> error(Function<Response, ? extends O> f)
    {
        mapFunctions.addStatusRangeFunction(
                new OrStatusRange(new HundredsStatusRange(HttpStatus.BAD_REQUEST),
                        new HundredsStatusRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);

        return this;
    }

    // 1xx, 3xx, 4xx and 5xx
    @Override
    public ResponseTransformation<O> notSuccessful(Function<Response, ? extends O> f)
    {
        mapFunctions.addStatusRangeFunction(new NotInStatusRange(new HundredsStatusRange(HttpStatus.OK)), f);
        return this;
    }

    @Override
    public ResponseTransformation<O> others(Function<Response, ? extends O> f)
    {
        mapFunctions.setOthersFunction(f);
        return this;
    }

    @Override
    public ResponseTransformation<O> otherwise(final Function<Throwable, O> callback)
    {
        others(new Function<Response, O>()
        {
            @Override
            public O apply(@Nullable Response input)
            {
                return callback.apply(new UnexpectedResponseException(input));
            }
        });
        fail(callback);
        return this;
    }

    @Override
    public ResponseTransformation<O> done(final Function<Response, O> f)
    {
        others(new Function<Response, O>()
        {
            @Override
            public O apply(@Nullable Response input)
            {
                return f.apply(input);
            }
        });
        return this;
    }

    @Override
    public ResponseTransformation<O> fail(Function<Throwable, ? extends O> f)
    {
        failFunction = f;
        return this;
    }

    private ResponseTransformation<O> addSingle(HttpStatus status, Function<Response, ? extends O> f)
    {
        return addSingle(status.code, f);
    }

    private ResponseTransformation<O> addSingle(int statusCode, Function<Response, ? extends O> f)
    {
        mapFunctions.addStatusRangeFunction(new SingleStatusRange(statusCode), f);
        return this;
    }

    private ResponseTransformation<O> addRange(HttpStatus status, Function<Response, ? extends O> f)
    {
        mapFunctions.addStatusRangeFunction(new HundredsStatusRange(status), f);
        return this;
    }

    @Override
    public O claim()
    {
        return toPromise().claim();
    }

    @Override
    public Promise<O> toPromise()
    {
        return responsePromise.fold(failFunction, mapFunctions);
    }

    private Function<Throwable, ? extends O> defaultThrowableHandler()
    {
        return new Function<Throwable, O>()
        {
            @Override
            public O apply(Throwable throwable)
            {
                if (throwable instanceof RuntimeException)
                {
                    throw (RuntimeException) throwable;
                }
                throw new ResponseTransformationException(throwable);
            }
        };
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
