package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Map;

import static com.atlassian.httpclient.api.ResponsePromiseMapFunction.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Maps.*;

@NotThreadSafe
public final class ResponsePromiseTransformationBuilder<O>
{
    private final BaseResponsePromise<Response> baseResponsePromise;

    private final Map<StatusRange, Function<Response, ? extends O>> functions;

    private Function<Response, ? extends O> othersFunction;

    private Function<Throwable, ? extends O> throwableHandler;

    ResponsePromiseTransformationBuilder(BaseResponsePromise<Response> baseResponsePromise)
    {
        this.baseResponsePromise = checkNotNull(baseResponsePromise);
        this.functions = newHashMap();
    }

    // 1xx
    public ResponsePromiseTransformationBuilder<O> informational(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.CONTINUE, f);
    }

    // 2xx
    public ResponsePromiseTransformationBuilder<O> successful(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.OK, f);
    }

    public ResponsePromiseTransformationBuilder<O> ok(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.OK, f);
    }

    public ResponsePromiseTransformationBuilder<O> created(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CREATED, f);
    }

    public ResponsePromiseTransformationBuilder<O> noContent(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NO_CONTENT, f);
    }

    // 3xx
    public ResponsePromiseTransformationBuilder<O> redirection(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.MULTIPLE_CHOICES, f);
    }

    public ResponsePromiseTransformationBuilder<O> seeOther(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SEE_OTHER, f);
    }

    public ResponsePromiseTransformationBuilder<O> notModified(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_MODIFIED, f);
    }

    // 4xx
    public ResponsePromiseTransformationBuilder<O> clientError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.BAD_REQUEST, f);
    }

    public ResponsePromiseTransformationBuilder<O> badRequest(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.BAD_REQUEST, f);
    }

    public ResponsePromiseTransformationBuilder<O> unauthorized(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.UNAUTHORIZED, f);
    }

    public ResponsePromiseTransformationBuilder<O> forbidden(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.FORBIDDEN, f);
    }

    public ResponsePromiseTransformationBuilder<O> notFound(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_FOUND, f);
    }

    public ResponsePromiseTransformationBuilder<O> conflict(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CONFLICT, f);
    }

    // 5xx
    public ResponsePromiseTransformationBuilder<O> serverError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    public ResponsePromiseTransformationBuilder<O> internalServerError(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    public ResponsePromiseTransformationBuilder<O> serviceUnavailable(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
    }

    // 4xx and 5xx
    public ResponsePromiseTransformationBuilder<O> error(Function<Response, ? extends O> f)
    {
        functions.put(new OrStatusRange(new HundredsStatusRange(HttpStatus.BAD_REQUEST), new HundredsStatusRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);
        return this;
    }

    // 1xx, 3xx, 4xx and 5xx
    public ResponsePromiseTransformationBuilder<O> notSuccessful(Function<Response, ? extends O> f)
    {
        functions.put(new NotInStatusRange(new HundredsStatusRange(HttpStatus.OK)), f);
        return this;
    }

    public ResponsePromiseTransformationBuilder<O> others(Function<Response, ? extends O> f)
    {
        othersFunction = f;
        return this;
    }

    public ResponsePromiseTransformationBuilder<O> fail(Function<Throwable, ? extends O> f)
    {
        throwableHandler = f;
        return this;
    }

    private ResponsePromiseTransformationBuilder<O> addSingle(HttpStatus status, Function<Response, ? extends O> f)
    {
        functions.put(new SingleStatusRange(status), f);
        return this;
    }

    private ResponsePromiseTransformationBuilder<O> addRange(HttpStatus status, Function<Response, ? extends O> f)
    {
        functions.put(new HundredsStatusRange(status), f);
        return this;
    }

    public Promise<O> toPromise()
    {
        return baseResponsePromise.fold(
                throwableHandler != null ? throwableHandler : defaultThrowableHandler(),
                new ResponsePromiseMapFunction<O>(functions, othersFunction));
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
        private final HttpStatus status;

        SingleStatusRange(HttpStatus status)
        {
            this.status = checkNotNull(status);
        }

        @Override
        public boolean isIn(int code)
        {
            return this.status.code == code;
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
