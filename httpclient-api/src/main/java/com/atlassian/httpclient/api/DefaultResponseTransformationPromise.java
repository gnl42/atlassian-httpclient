package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.atlassian.httpclient.api.ResponsePromiseMapFunction.*;
import static com.google.common.base.Preconditions.*;

@NotThreadSafe
final class DefaultResponseTransformationPromise<O> implements ResponseTransformationPromise<O>

{
    private volatile Promise<O> delegate;
    private final ResponsePromise responsePromise;

    private SingleMatchDelegatingFunction<Throwable, Function<Throwable, ? extends O>> failFunction;
    private SingleMatchDelegatingFunction<Response, ResponsePromiseMapFunction<O>> doneFunction;

    DefaultResponseTransformationPromise(ResponsePromise baseResponsePromise)
    {
        checkNotNull(baseResponsePromise);
        this.responsePromise = baseResponsePromise;
        this.failFunction = new SingleMatchDelegatingFunction
                <Throwable, Function<Throwable, ? extends O>>(defaultThrowableHandler());
        this.doneFunction = new SingleMatchDelegatingFunction<Response, ResponsePromiseMapFunction<O>>(
                new ResponsePromiseMapFunction<O>());
    }

    /**
     * Register a function to transform a HTTP response with a specific status code.
     * Use this as a fallback if the status code you're interested in does not have
     * a more explicit registration method for it.
     *
     * @param statusCode The code to select on
     * @param f The transforming function
     * @return This instance for chaining
     */
    @Override
    public ResponseTransformationPromise<O> on(int statusCode, Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.fromCode(statusCode), f);
    }

    @Override
    public ResponseTransformationPromise<O> informational(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.CONTINUE, f);
    }

    // 2xx
    @Override
    public ResponseTransformationPromise<O> successful(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.OK, f);
    }

    @Override
    public ResponseTransformationPromise<O> ok(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.OK, f);
    }

    @Override
    public ResponseTransformationPromise<O> created(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CREATED, f);
    }

    @Override
    public ResponseTransformationPromise<O> noContent(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NO_CONTENT, f);
    }

    // 3xx
    @Override
    public ResponseTransformationPromise<O> redirection(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.MULTIPLE_CHOICES, f);
    }

    @Override
    public ResponseTransformationPromise<O> seeOther(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SEE_OTHER, f);
    }

    @Override
    public ResponseTransformationPromise<O> notModified(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_MODIFIED, f);
    }

    // 4xx
    @Override
    public ResponseTransformationPromise<O> clientError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public ResponseTransformationPromise<O> badRequest(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.BAD_REQUEST, f);
    }

    @Override
    public ResponseTransformationPromise<O> unauthorized(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.UNAUTHORIZED, f);
    }

    @Override
    public ResponseTransformationPromise<O> forbidden(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.FORBIDDEN, f);
    }

    @Override
    public ResponseTransformationPromise<O> notFound(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.NOT_FOUND, f);
    }

    @Override
    public ResponseTransformationPromise<O> conflict(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.CONFLICT, f);
    }

    // 5xx
    @Override
    public ResponseTransformationPromise<O> serverError(Function<Response, ? extends O> f)
    {
        return addRange(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public ResponseTransformationPromise<O> internalServerError(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.INTERNAL_SERVER_ERROR, f);
    }

    @Override
    public ResponseTransformationPromise<O> serviceUnavailable(Function<Response, ? extends O> f)
    {
        return addSingle(HttpStatus.SERVICE_UNAVAILABLE, f);
    }

    // 4xx and 5xx
    @Override
    public ResponseTransformationPromise<O> error(Function<Response, ? extends O> f)
    {
        doneFunction.delegate.addStatusRangeFunction(
                new OrStatusRange(new HundredsStatusRange(HttpStatus.BAD_REQUEST),
                        new HundredsStatusRange(HttpStatus.INTERNAL_SERVER_ERROR)), f);

        applyFold();
        return this;
    }

    private void applyFold()
    {
        if (!doneFunction.isMatched() && !failFunction.isMatched())
        {
            delegate = responsePromise.fold(failFunction, doneFunction);
        }
    }

    // 1xx, 3xx, 4xx and 5xx
    @Override
    public ResponseTransformationPromise<O> notSuccessful(Function<Response, ? extends O> f)
    {
        doneFunction.delegate.addStatusRangeFunction(
                new NotInStatusRange(new HundredsStatusRange(HttpStatus.OK)), f);

        applyFold();
        return this;
    }

    @Override
    public ResponseTransformationPromise<O> others(Function<Response, ? extends O> f)
    {
        doneFunction.delegate.setOthersFunction(f);
        applyFold();
        return this;
    }

    @Override
    public ResponseTransformationPromise<O> otherwise(final Function<Throwable, O> callback)
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
    public ResponseTransformationPromise<O> done(final Function<Response, O> f)
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
    public ResponseTransformationPromise<O> fail(Function<Throwable, ? extends O> f)
    {
        failFunction.setDelegate(f);
        applyFold();
        return this;
    }

    private ResponseTransformationPromise<O> addSingle(HttpStatus status, Function<Response, ? extends O> f)
    {
        doneFunction.delegate.addStatusRangeFunction(new SingleStatusRange(status), f);
        applyFold();
        return this;
    }

    private ResponseTransformationPromise<O> addRange(HttpStatus status, Function<Response, ? extends O> f)
    {
        doneFunction.delegate.addStatusRangeFunction(new HundredsStatusRange(status), f);
        applyFold();
        return this;
    }

    @Override
    public O claim()
    {
        return delegate.claim();
    }

    public Promise<O> done(Effect<O> oEffect)
    {
        return delegate.done(oEffect);
    }

    @Override
    public Promise<O> fail(Effect<Throwable> throwableEffect)
    {
        return delegate.fail(throwableEffect);
    }

    public Promise<O> then(FutureCallback<O> oFutureCallback)
    {
        return delegate.then(oFutureCallback);
    }

    public <B> Promise<B> map(Function<? super O, ? extends B> function)
    {
        return delegate.map(function);
    }

    public <B> Promise<B> flatMap(Function<? super O, Promise<B>> promiseFunction)
    {
        return delegate.flatMap(promiseFunction);
    }

    public Promise<O> recover(Function<Throwable, ? extends O> throwableFunction)
    {
        return delegate.recover(throwableFunction);
    }

    public <B> Promise<B> fold(Function<Throwable, ? extends B> throwableFunction,
            Function<? super O, ? extends B> function)
    {
        return delegate.fold(throwableFunction, function);
    }

    @Override
    public void addListener(Runnable listener, Executor executor)
    {
        delegate.addListener(listener, executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return delegate.isDone();
    }

    @Override
    public O get() throws InterruptedException, ExecutionException
    {
        return delegate.get();
    }

    @Override
    public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException
    {
        return delegate.get(timeout, unit);
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

    /**
     * This class memorizes a single match, whether it was the target object, a runtime exception,
     * or an error, and replays no many times it is invoked.
     */
    final class SingleMatchDelegatingFunction<INPUT, DELEGATE extends Function<INPUT, ? extends O>> implements Function<INPUT, O>
    {
        private DELEGATE delegate;
        private volatile O matchValue;
        private volatile RuntimeException thrownExceptionMatch;
        private volatile Error thrownErrorMatch;
        private volatile boolean match;

        public SingleMatchDelegatingFunction(DELEGATE delegateDefault)
        {
            delegate = delegateDefault;
        }

        @Override
        public O apply(@Nullable INPUT input)
        {
            if (thrownErrorMatch != null)
            {
                throw thrownErrorMatch;
            }
            else if (thrownExceptionMatch != null)
            {
                throw thrownExceptionMatch;
            }
            else if (match)
            {
                return matchValue;
            }

            try
            {
                matchValue = delegate.apply(input);
                match = true;
                return matchValue;
            }
            catch (RuntimeException ex)
            {
                thrownExceptionMatch = ex;
                throw ex;
            }
            catch (Error err)
            {
                thrownErrorMatch = err;
                throw err;
            }
        }

        public void setDelegate(DELEGATE delegate)
        {
            this.delegate = delegate;
        }

        public boolean isMatched()
        {
            return match || thrownErrorMatch != null | thrownExceptionMatch != null;
        }
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
