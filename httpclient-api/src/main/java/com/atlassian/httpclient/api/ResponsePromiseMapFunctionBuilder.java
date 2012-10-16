package com.atlassian.httpclient.api;

import com.google.common.base.Function;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Map;

import static com.atlassian.httpclient.api.ResponsePromiseMapFunction.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Maps.*;

@NotThreadSafe
public final class ResponsePromiseMapFunctionBuilder<O>
{
    private final Map<StatusRange, Function<Response, ? extends O>> functions;

    private Function<Response, ? extends O> othersFunction;

    ResponsePromiseMapFunctionBuilder()
    {
        functions = newHashMap();
    }

    // 1xx
    public ResponsePromiseMapFunctionBuilder<O> informational(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.CONTINUE, f);
    }

    // 2xx
    public ResponsePromiseMapFunctionBuilder<O> successful(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.OK, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> ok(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.OK, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> created(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CREATED, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> noContent(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NO_CONTENT, f);
    }

    // 3xx
    public ResponsePromiseMapFunctionBuilder<O> redirection(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.MULTIPLE_CHOICES, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> seeOther(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SEE_OTHER, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> notModified(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_MODIFIED, f);
    }

    // 4xx
    public ResponsePromiseMapFunctionBuilder<O> clientError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.BAD_REQUEST, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> badRequest(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.BAD_REQUEST, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> unauthorized(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.UNAUTHORIZED, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> forbidden(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.FORBIDDEN, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> notFound(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_FOUND, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> conflict(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CONFLICT, f);
    }

    // 5xx
    public ResponsePromiseMapFunctionBuilder<O> serverError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> internalServerError(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    public ResponsePromiseMapFunctionBuilder<O> serviceUnavailable(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
    }

    // 4xx and 5xx
    public ResponsePromiseMapFunctionBuilder<O> error(Function<Response, ? extends O> f)
    {
        functions.put(new OrStatusRange(new HundredsStatusRange(HttpStatus.BAD_REQUEST), new HundredsStatusRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);
        return this;
    }

    // 1xx, 3xx, 4xx and 5xx
    public ResponsePromiseMapFunctionBuilder<O> notSuccessful(Function<Response, ? extends O> f)
    {
        functions.put(new NotInStatusRange(new HundredsStatusRange(HttpStatus.OK)), f);
        return this;
    }

    public ResponsePromiseMapFunctionBuilder<O> others(Function<Response, ? extends O> f)
    {
        othersFunction = f;
        return this;
    }

    private ResponsePromiseMapFunctionBuilder<O> addSingle(HttpStatus status, Function<Response, ? extends O> f)
    {
        functions.put(new SingleStatusRange(status), f);
        return this;
    }

    private ResponsePromiseMapFunctionBuilder<O> addRange(HttpStatus status, Function<Response, ? extends O> f)
    {
        functions.put(new HundredsStatusRange(status), f);
        return this;
    }

    public Function<? super Response, O> build()
    {
        return new ResponsePromiseMapFunction<O>(functions, othersFunction);
    }

    private static final class SingleStatusRange implements StatusRange
    {
        private final HttpStatus status;

        private SingleStatusRange(HttpStatus status)
        {
            this.status = checkNotNull(status);
        }

        @Override
        public boolean isIn(int code)
        {
            return this.status.code == code;
        }
    }

    private static final class HundredsStatusRange implements StatusRange
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

    private static final class NotInStatusRange implements StatusRange
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

    private static final class OrStatusRange implements StatusRange
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
