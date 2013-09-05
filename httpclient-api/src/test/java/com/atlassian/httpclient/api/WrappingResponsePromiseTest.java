package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;

import static com.atlassian.util.concurrent.Promises.*;
import static org.junit.Assert.*;

@RunWith (MockitoJUnitRunner.class)
public final class WrappingResponsePromiseTest
{
    @Mock
    private Response response;

    @Test
    public final void testThatWhenMapFunctionThrowsExceptionThenMappedPromiseIsFailWithException()
    {
        final String message = "This is the message for the test!";

        final SettableFuture<Response> future = SettableFuture.create();
        final Promise<Response> responsePromise = forListenableFuture(future);

        final OnTimeEffect onFail = new OnTimeEffect()
        {
            @Override
            void doApply(Throwable t)
            {
                assertEquals(message, t.getMessage());
            }
        };

        final Promise<Object> mappedPromise = responsePromise.map(newExceptionFunction(message)).fail(onFail);

        future.set(response);

        assertTrue(onFail.isCalled());
        assertTrue(mappedPromise.isDone());
    }

    private <I, O> ExceptionThrowingFunction<I, O> newExceptionFunction(String message)
    {
        return new ExceptionThrowingFunction<I, O>(message);
    }

    private static abstract class OnTimeEffect implements Effect<Throwable>
    {
        private boolean called = false;

        @Override
        public void apply(Throwable t)
        {
            if (called)
            {
                throw new IllegalStateException("This effect method already has been called!");
            }
            called = true;
            doApply(t);
        }

        abstract void doApply(Throwable t);

        boolean isCalled()
        {
            return called;
        }
    }

    private static final class ExceptionThrowingFunction<I, O> implements Function<I, O>
    {
        private final String message;

        public ExceptionThrowingFunction(String message)
        {
            this.message = message;
        }

        @Override
        public O apply(@Nullable I input)
        {
            throw new RuntimeException(message);
        }
    }
}
