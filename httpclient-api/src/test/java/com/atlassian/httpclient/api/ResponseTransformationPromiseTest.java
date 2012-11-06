package com.atlassian.httpclient.api;

import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.atlassian.httpclient.api.ResponsePromises.toResponsePromise;
import static com.atlassian.util.concurrent.Promises.forListenableFuture;
import static com.google.common.collect.Iterables.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class ResponseTransformationPromiseTest
{
    private ResponseTransformationPromise<Object> promise;

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
        allFunctions = ImmutableSet.<Function<Response, Object>>builder()
                .add(informationalFunction)
                .add(successfulFunction)
                .add(okFunction)
                .add(noContentFunction)
                .add(createdFunction)
                .add(redirectionFunction)
                .add(clientErrorFunction)
                .add(notFoundFunction)
                .add(serverErrorFunction)
                .add(othersFunction)
                .build();
    }

    @Test
    public void testInformationalFunctionCalledOn1xx()
    {
        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < HttpStatus.OK.code; statusCode++)
        {
            testFunctionCalledForStatus(rangesBuilder(), informationalFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInformationalFunctionCalledOn2xx()
    {
        for (int statusCode = HttpStatus.OK.code; statusCode < HttpStatus.MULTIPLE_CHOICES.code; statusCode++)
        {
            testFunctionCalledForStatus(rangesBuilder(), successfulFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testOkFunctionCalledOn200()
    {
        testFunctionCalledForStatus(okFunction, HttpStatus.OK.code);
    }

    @Test
    public void testCreatedFunctionCalledOn201()
    {
        testFunctionCalledForStatus(createdFunction, HttpStatus.CREATED.code);
    }

    @Test
    public void testNoContentFunctionCalledOn204()
    {
        testFunctionCalledForStatus(noContentFunction, HttpStatus.NO_CONTENT.code);
    }


    @Test
    public void testInformationalFunctionCalledOn3xx()
    {
        for (int statusCode = HttpStatus.MULTIPLE_CHOICES.code; statusCode < HttpStatus.BAD_REQUEST.code; statusCode++)
        {
            testFunctionCalledForStatus(rangesBuilder(), redirectionFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testSeeOtherFunctionCalledOn303()
    {
        testFunctionCalledForStatus(seeOtherFunction, HttpStatus.SEE_OTHER.code);
    }

    @Test
    public void testSeeOtherFunctionCalledOn304()
    {
        testFunctionCalledForStatus(notModifiedFunction, HttpStatus.NOT_MODIFIED.code);
    }

    @Test
    public void testInformationalFunctionCalledOn4xx()
    {
        for (int statusCode = HttpStatus.BAD_REQUEST.code; statusCode < HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode++)
        {
            testFunctionCalledForStatus(rangesBuilder(), clientErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testBadRequestFunctionCalledOn400()
    {
        testFunctionCalledForStatus(badRequestFunction, HttpStatus.BAD_REQUEST.code);
    }

    @Test
    public void testBadRequestFunctionCalledOn401()
    {
        testFunctionCalledForStatus(unauthorizedFunction, HttpStatus.UNAUTHORIZED.code);
    }

    @Test
    public void testForbiddenFunctionCalledOn403()
    {
        testFunctionCalledForStatus(forbiddenFunction, HttpStatus.FORBIDDEN.code);
    }

    @Test
    public void testNotFoundFunctionCalledOn404()
    {
        testFunctionCalledForStatus(notFoundFunction, HttpStatus.NOT_FOUND.code);
    }

    @Test
    public void testConflictFunctionCalledOn409()
    {
        testFunctionCalledForStatus(conflictFunction, HttpStatus.CONFLICT.code);
    }

    @Test
    public void testInformationalFunctionCalledOn5xx()
    {
        for (int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode < 600; statusCode++)
        {
            testFunctionCalledForStatus(rangesBuilder(), serverErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInternalServerErrorFunctionCalledOn500()
    {
        testFunctionCalledForStatus(internalServerErrorFunction, HttpStatus.INTERNAL_SERVER_ERROR.code);
    }

    @Test
    public void testServiceUnavailableFunctionCalledOn503()
    {
        testFunctionCalledForStatus(serviceUnavailableFunction, HttpStatus.SERVICE_UNAVAILABLE.code);
    }

    @Test(expected = IllegalStateException.class)
    public void testFailThrowsException()
    {
        Promise<Object> promise = newBuilder().fail(new Function<Throwable, Object>()
        {
            @Override
            public Object apply(@Nullable Throwable input)
            {
                throw new IllegalStateException("foo");
            }
        });
        responseSettableFuture.setException(new RuntimeException());
        promise.claim();
    }

    @Test
    public void testDelayedExecutionExecutesOnce()
    {
        final AtomicInteger counter = new AtomicInteger(0);
        ResponseTransformationPromise<String> responsePromise =
                new DefaultResponseTransformationPromise<String>(toResponsePromise(forListenableFuture(responseSettableFuture)));
        when(response.getStatusCode()).thenReturn(200);

        Promise<String> promise = responsePromise
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(@Nullable Response input)
                    {
                        return "foo" + counter.getAndIncrement();
                    }
                })
                .fail(new Function<Throwable, String>()
                {
                    @Override
                    public String apply(@Nullable Throwable input)
                    {
                        throw new IllegalStateException();
                    }
                });
        responseSettableFuture.set(response);
        assertEquals("foo0", promise.claim());
    }

    @Test
    public void testDelayedExecutionExecutesDoneOnceWithException()
    {
        final AtomicInteger counter = new AtomicInteger(0);
        ResponseTransformationPromise<String> responsePromise =
                new DefaultResponseTransformationPromise<String>(toResponsePromise(forListenableFuture(responseSettableFuture)));
        when(response.getStatusCode()).thenReturn(200);

        Promise<String> promise = responsePromise
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(@Nullable Response input)
                    {
                        throw new IllegalArgumentException("foo" + counter.getAndIncrement());
                    }
                })
                .fail(new Function<Throwable, String>()
                {
                    @Override
                    public String apply(@Nullable Throwable input)
                    {
                        return null;
                    }
                });
        responseSettableFuture.set(response);
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
        ResponseTransformationPromise<String> responsePromise =
                new DefaultResponseTransformationPromise<String>(toResponsePromise(forListenableFuture(responseSettableFuture)));
        when(response.getStatusCode()).thenReturn(200);

        Promise<String> promise = responsePromise
                .fail(new Function<Throwable, String>()
                {
                    @Override
                    public String apply(@Nullable Throwable input)
                    {
                        return "foo" + counter.getAndIncrement();
                    }
                })
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(@Nullable Response input)
                    {
                        throw new IllegalStateException();
                    }
                });
        responseSettableFuture.set(response);
        assertEquals("foo0", promise.claim());
    }

    @Test
    public void testNotSuccessfulFunctionCalled()
    {


        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++)
        {
            final ResponseTransformationPromise<Object> promise = newBuilder()
                    .notSuccessful(clientErrorFunction)
                    .others(successfulFunction);
            if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code)
            {
                testFunctionCalledForStatus(promise, successfulFunction, statusCode);
            }
            else
            {
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
            final ResponseTransformationPromise<Object> promise = newBuilder()
                    .error(clientErrorFunction)
                    .others(successfulFunction);
            if (HttpStatus.BAD_REQUEST.code <= statusCode && statusCode < 600)
            {
                testFunctionCalledForStatus(promise, clientErrorFunction, statusCode);
            }
            else
            {
                testFunctionCalledForStatus(promise, successfulFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testOthersFunctionCalled()
    {
        testFunctionCalledForStatus(
                newBuilder().others(othersFunction),
                othersFunction,
                HttpStatus.OK.code);
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

    @Test
    public void testFailCanTransformExceptions()
    {
        responseSettableFuture.setException(new Throwable("Some message"));
        assertEquals("Some message", newBuilder()
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(Response input)
                    {
                        return "Ok";
                    }
                })
                .fail(new Function<Throwable, String>()
                {
                    @Override
                    public String apply(Throwable input)
                    {
                        return input.getMessage();
                    }
                }).claim());
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testFailExceptionsWithoutHandlersAreThrownAtClaim()
    {
        responseSettableFuture.setException(new IllegalMonitorStateException("Some message"));
        newBuilder().claim();
    }

    private void testFunctionCalledForStatus(Function<Response, Object> function, int statusCode)
    {
        testFunctionCalledForStatus(
                newBuilder()
                        .informational(informationalFunction)
                        .ok(okFunction)
                        .created(createdFunction)
                        .noContent(noContentFunction)
                        .notFound(notFoundFunction)
                        .seeOther(seeOtherFunction)
                        .notModified(notModifiedFunction)
                        .badRequest(badRequestFunction)
                        .unauthorized(unauthorizedFunction)
                        .forbidden(forbiddenFunction)
                        .conflict(conflictFunction)
                        .internalServerError(internalServerErrorFunction)
                        .serviceUnavailable(serviceUnavailableFunction)
                        .others(othersFunction),
                function,
                statusCode);
    }

    private void testFunctionCalledForStatus(ResponseTransformationPromise<Object> promise, Function<Response, Object> function, int statusCode)
    {
        when(response.getStatusCode()).thenReturn(statusCode);

        responseSettableFuture.set(response);

        verify(function).apply(response);
        verifyNoMoreInteractions(allFunctionsAsArray());
    }

    private ResponseTransformationPromise<Object> newBuilder()
    {
        return new DefaultResponseTransformationPromise<Object>(toResponsePromise(forListenableFuture(responseSettableFuture)));
    }

    private ResponseTransformationPromise<Object> rangesBuilder()
    {
        return newBuilder()
                .successful(successfulFunction)
                .informational(informationalFunction)
                .redirection(redirectionFunction)
                .clientError(clientErrorFunction)
                .serverError(serverErrorFunction)
                .others(othersFunction);
    }

    private Function[] allFunctionsAsArray()
    {
        return toArray(allFunctions, Function.class);
    }

    private void resetAllMocks()
    {
        Mockito.reset(allFunctionsAsArray());
        responseSettableFuture = SettableFuture.create();
    }
}
