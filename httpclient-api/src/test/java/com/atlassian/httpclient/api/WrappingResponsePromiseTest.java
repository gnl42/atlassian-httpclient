package com.atlassian.httpclient.api;

import io.atlassian.util.concurrent.Promise;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.atlassian.util.concurrent.Promises.forCompletionStage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class WrappingResponsePromiseTest {
    @Mock
    private Response response;

    @Test
    public final void testThatWhenMapFunctionThrowsExceptionThenMappedPromiseIsFailWithException() {
        final String message = "This is the message for the test!";

        final CompletableFuture<Response> future = new CompletableFuture<>();
        final ResponsePromise responsePromise = new WrappingResponsePromise(forCompletionStage(future));

        final OnTimeEffect onFail = new OnTimeEffect() {
            @Override
            void doApply(Throwable t) {
                assertEquals(message, t.getMessage());
            }
        };

        final Promise<Object> mappedPromise = responsePromise.map(newExceptionFunction(message)).fail(onFail);

        future.complete(response);

        assertTrue(onFail.isCalled());
        assertTrue(mappedPromise.isDone());
    }

    private <I, O> ExceptionThrowingFunction<I, O> newExceptionFunction(String message) {
        return new ExceptionThrowingFunction<>(message);
    }

    private static abstract class OnTimeEffect implements Consumer<Throwable> {
        private boolean called;

        @Override
        public void accept(Throwable t) {
            if (called) {
                throw new IllegalStateException("This effect method already has been called!");
            }
            called = true;
            doApply(t);
        }

        abstract void doApply(Throwable t);

        boolean isCalled() {
            return called;
        }
    }

    private static final class ExceptionThrowingFunction<I, O> implements Function<I, O> {
        private final String message;

        ExceptionThrowingFunction(String message) {
            this.message = message;
        }

        @Override
        public O apply(@Nullable I input) {
            throw new RuntimeException(message);
        }
    }
}
