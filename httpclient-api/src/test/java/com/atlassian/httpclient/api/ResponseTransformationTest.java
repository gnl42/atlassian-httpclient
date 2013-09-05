package com.atlassian.httpclient.api;

import com.atlassian.httpclient.api.ResponseTransformation.Builder;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

import static com.atlassian.util.concurrent.Promises.forListenableFuture;
import static com.google.common.collect.Iterables.toArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public final class ResponseTransformationTest
{
    @Mock
    private Response response;

    @Mock
    private Function<Response, Object> informationalFunction;

    @Mock
    private Function<Response, Object> successfulFunction;

    @Mock
    private Function<Response, Object> okFunction;

    @Mock
    private Function<Response, Object> createdFunction;

    @Mock
    private Function<Response, Object> noContentFunction;

    @Mock
    private Function<Response, Object> redirectionFunction;

    @Mock
    private Function<Response, Object> clientErrorFunction;

    @Mock
    private Function<Response, Object> notFoundFunction;

    @Mock
    private Function<Response, Object> seeOtherFunction;

    @Mock
    private Function<Response, Object> notModifiedFunction;

    @Mock
    private Function<Response, Object> badRequestFunction;

    @Mock
    private Function<Response, Object> unauthorizedFunction;

    @Mock
    private Function<Response, Object> forbiddenFunction;

    @Mock
    private Function<Response, Object> conflictFunction;

    @Mock
    private Function<Response, Object> internalServerErrorFunction;

    @Mock
    private Function<Response, Object> serviceUnavailableFunction;

    @Mock
    private Function<Response, Object> serverErrorFunction;

    @Mock
    private Function<Response, Object> othersFunction;

    private Set<Function<Response, Object>> allFunctions;

    private SettableFuture<Response> responseSettableFuture;

    @Before
    public void setUp()
    {
        responseSettableFuture = SettableFuture.create();
        allFunctions = ImmutableSet.<Function<Response, Object>>builder().add(informationalFunction).add(successfulFunction).add(okFunction)
                .add(noContentFunction).add(createdFunction).add(redirectionFunction).add(clientErrorFunction).add(notFoundFunction).add(serverErrorFunction)
                .add(othersFunction).build();
    }

    @Test
    public void testInformationalFunctionCalledOn1xx()
    {
        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < HttpStatus.OK.code; statusCode++)
        {
            when(informationalFunction.apply(response)).thenReturn(new Object());
            testFunctionCalledForStatus(rangesBuilder(), informationalFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInformationalFunctionCalledOn2xx()
    {
        for (int statusCode = HttpStatus.OK.code; statusCode < HttpStatus.MULTIPLE_CHOICES.code; statusCode++)
        {
            when(successfulFunction.apply(response)).thenReturn(new Object());
            testFunctionCalledForStatus(rangesBuilder(), successfulFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testOkFunctionCalledOn200()
    {
        when(okFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(okFunction, HttpStatus.OK.code);
    }

    @Test
    public void testCreatedFunctionCalledOn201()
    {
        when(createdFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(createdFunction, HttpStatus.CREATED.code);
    }

    @Test
    public void testNoContentFunctionCalledOn204()
    {
        when(noContentFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(noContentFunction, HttpStatus.NO_CONTENT.code);
    }

    @Test
    public void testInformationalFunctionCalledOn3xx()
    {
        for (int statusCode = HttpStatus.MULTIPLE_CHOICES.code; statusCode < HttpStatus.BAD_REQUEST.code; statusCode++)
        {
            when(redirectionFunction.apply(response)).thenReturn(new Object());
            testFunctionCalledForStatus(rangesBuilder(), redirectionFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testSeeOtherFunctionCalledOn303()
    {
        when(seeOtherFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(seeOtherFunction, HttpStatus.SEE_OTHER.code);
    }

    @Test
    public void testSeeOtherFunctionCalledOn304()
    {
        when(notModifiedFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(notModifiedFunction, HttpStatus.NOT_MODIFIED.code);
    }

    @Test
    public void testInformationalFunctionCalledOn4xx()
    {
        for (int statusCode = HttpStatus.BAD_REQUEST.code; statusCode < HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode++)
        {
            when(clientErrorFunction.apply(response)).thenReturn(new Object());
            testFunctionCalledForStatus(rangesBuilder(), clientErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testBadRequestFunctionCalledOn400()
    {
        when(badRequestFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(badRequestFunction, HttpStatus.BAD_REQUEST.code);
    }

    @Test
    public void testBadRequestFunctionCalledOn401()
    {
        when(unauthorizedFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(unauthorizedFunction, HttpStatus.UNAUTHORIZED.code);
    }

    @Test
    public void testForbiddenFunctionCalledOn403()
    {
        when(forbiddenFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(forbiddenFunction, HttpStatus.FORBIDDEN.code);
    }

    @Test
    public void testNotFoundFunctionCalledOn404()
    {
        when(notFoundFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(notFoundFunction, HttpStatus.NOT_FOUND.code);
    }

    @Test
    public void testConflictFunctionCalledOn409()
    {
        when(conflictFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(conflictFunction, HttpStatus.CONFLICT.code);
    }

    @Test
    public void testInformationalFunctionCalledOn5xx()
    {
        for (int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode < 600; statusCode++)
        {
            when(serverErrorFunction.apply(response)).thenReturn(new Object());
            testFunctionCalledForStatus(rangesBuilder(), serverErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInternalServerErrorFunctionCalledOn500()
    {
        when(internalServerErrorFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(internalServerErrorFunction, HttpStatus.INTERNAL_SERVER_ERROR.code);
    }

    @Test
    public void testServiceUnavailableFunctionCalledOn503()
    {
        when(serviceUnavailableFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(serviceUnavailableFunction, HttpStatus.SERVICE_UNAVAILABLE.code);
    }

    @Test (expected = IllegalStateException.class)
    public void testFailThrowsException()
    {
        Promise<Object> promise = newBuilder().fail(new Function<Throwable, Object>()
        {
            @Override
            public Object apply(@Nullable Throwable input)
            {
                throw new IllegalStateException("foo");
            }
        }).build().transform(Promises.forListenableFuture(responseSettableFuture));
        responseSettableFuture.setException(new RuntimeException());
        promise.claim();
    }

    @Test
    public void testDelayedExecutionExecutesOnce()
    {
        final AtomicInteger counter = new AtomicInteger(0);
        when(this.response.statusCode()).thenReturn(200);
        Promise<String> promise = ResponseTransformation.Builder.<String>builder().ok(new Function<Response, String>()
        {
            @Override
            public String apply(@Nullable Response input)
            {
                return "foo" + counter.getAndIncrement();
            }
        }).fail(new Function<Throwable, String>()
        {
            @Override
            public String apply(@Nullable Throwable input)
            {
                throw new IllegalStateException();
            }
        }).build().transform(forListenableFuture(responseSettableFuture));
        responseSettableFuture.set(this.response);
        assertEquals("foo0", promise.claim());
    }

    @Test
    public void testDelayedExecutionExecutesDoneOnceWithException()
    {
        final AtomicInteger counter = new AtomicInteger(0);
        when(this.response.statusCode()).thenReturn(200);

        Promise<String> promise = Builder.<String>builder().ok(new Function<Response, String>()
        {
            @Override
            public String apply(@Nullable Response input)
            {
                throw new IllegalArgumentException("foo" + counter.getAndIncrement());
            }
        }).fail(new Function<Throwable, String>()
        {
            @Override
            public String apply(@Nullable Throwable input)
            {
                return null;
            }
        }).build().transform(forListenableFuture(responseSettableFuture));
        responseSettableFuture.set(this.response);
        try
        {
            promise.claim();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("foo0", ex.getMessage());
        }
    }

    @Test
    public void testDelayedExecutionExecutesFailOnce()
    {
        final AtomicInteger counter = new AtomicInteger(0);
        when(this.response.statusCode()).thenReturn(200);

        Promise<String> promise = Builder.<String>builder().fail(new Function<Throwable, String>()
        {
            @Override
            public String apply(@Nullable Throwable input)
            {
                return "foo" + counter.getAndIncrement();
            }
        }).ok(new Function<Response, String>()
        {
            @Override
            public String apply(@Nullable Response input)
            {
                throw new IllegalStateException();
            }
        }).build().transform(forListenableFuture(responseSettableFuture));
        responseSettableFuture.set(this.response);
        assertEquals("foo0", promise.claim());
    }

    @Test
    public void testNotSuccessfulFunctionCalled()
    {

        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++)
        {
            final ResponseTransformation<Object> promise = Builder.builder().notSuccessful(clientErrorFunction).others(successfulFunction).build();
            if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code)
            {
                when(successfulFunction.apply(response)).thenReturn(new Object());
                testFunctionCalledForStatus(promise, successfulFunction, statusCode);
            }
            else
            {
                when(clientErrorFunction.apply(response)).thenReturn(new Object());
                testFunctionCalledForStatus(promise, clientErrorFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testErrorFunctionCalled()
    {

        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++)
        {
            final ResponseTransformation<Object> promise = Builder.builder().error(clientErrorFunction).others(successfulFunction).build();
            if (HttpStatus.BAD_REQUEST.code <= statusCode && statusCode < 600)
            {
                when(clientErrorFunction.apply(response)).thenReturn(new Object());
                testFunctionCalledForStatus(promise, clientErrorFunction, statusCode);
            }
            else
            {
                when(successfulFunction.apply(response)).thenReturn(new Object());
                testFunctionCalledForStatus(promise, successfulFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testOthersFunctionCalled()
    {
        when(othersFunction.apply(response)).thenReturn(new Object());
        testFunctionCalledForStatus(Builder.builder().others(othersFunction).build(), othersFunction, HttpStatus.OK.code);
    }

    @Test
    public void testFunctionAddedOnNonStandardHttpStatus()
    {
        newBuilder().on(601, new Function<Response, Object>()
        {
            @Override
            public Object apply(@Nullable Response input)
            {
                return null;
            }
        });
    }

    @Test (expected = IllegalMonitorStateException.class)
    public void testFailCanThrowExceptions()
    {
        responseSettableFuture.setException(new Throwable("Some message"));
        Builder.builder().ok(new Function<Response, String>()
        {
            @Override
            public String apply(Response input)
            {
                return "Ok";
            }
        }).fail(new Function<Throwable, String>()
        {
            @Override
            public String apply(Throwable input)
            {
                throw new IllegalMonitorStateException();
            }
        }).build().transform(forListenableFuture(responseSettableFuture)).claim();
    }

    @Test (expected = IllegalMonitorStateException.class)
    public void testFailExceptionsWithoutHandlersAreThrownAtClaim()
    {
        responseSettableFuture.setException(new IllegalMonitorStateException("Some message"));
        Builder.builder().build().transform(forListenableFuture(responseSettableFuture)).claim();
    }

    private void testFunctionCalledForStatus(Function<Response, Object> function, int statusCode)
    {
        testFunctionCalledForStatus(
                Builder.builder().informational(informationalFunction).ok(okFunction).created(createdFunction).noContent(noContentFunction)
                        .notFound(notFoundFunction).seeOther(seeOtherFunction).notModified(notModifiedFunction).badRequest(badRequestFunction)
                        .unauthorized(unauthorizedFunction).forbidden(forbiddenFunction).conflict(conflictFunction).internalServerError(internalServerErrorFunction)
                        .serviceUnavailable(serviceUnavailableFunction).others(othersFunction).build(), function, statusCode);
    }

    private void testFunctionCalledForStatus(ResponseTransformation<Object> transformer, Function<Response, Object> function, int statusCode)
    {
        when(response.statusCode()).thenReturn(statusCode);
        responseSettableFuture.set(response);

        transformer.transform(forListenableFuture(responseSettableFuture)).claim();

        verify(function).apply(response);
        verifyNoMoreInteractions(allFunctionsAsArray());
    }

    private ResponseTransformation.Builder<Object> newBuilder()
    {
        return ResponseTransformation.Builder.builder();
    }

    private ResponseTransformation<Object> rangesBuilder()
    {
        return newBuilder().successful(successfulFunction).informational(informationalFunction).redirection(redirectionFunction)
                .clientError(clientErrorFunction).serverError(serverErrorFunction).others(othersFunction).build();
    }

    private Object[] allFunctionsAsArray()
    {
        return toArray(allFunctions, Function.class);
    }

    private void resetAllMocks()
    {
        Mockito.reset(allFunctionsAsArray());
        responseSettableFuture = SettableFuture.create();
    }
}
