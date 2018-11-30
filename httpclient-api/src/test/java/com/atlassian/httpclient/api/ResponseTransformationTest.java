package com.atlassian.httpclient.api;

import com.google.common.collect.ImmutableSet;
import io.atlassian.util.concurrent.Promise;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.atlassian.httpclient.api.ResponsePromises.toResponsePromise;
import static com.google.common.collect.Iterables.toArray;
import static io.atlassian.util.concurrent.Promises.forCompletionStage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ResponseTransformationTest {
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

    private CompletableFuture<Response> responseSettableFuture;

    @Before
    public void setUp() {
        responseSettableFuture = new CompletableFuture<>();
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
    public void testInformationalFunctionCalledOn1xx() {
        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < HttpStatus.OK.code; statusCode++) {
            testFunctionCalledForStatus(rangesBuilder(), informationalFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInformationalFunctionCalledOn2xx() {
        for (int statusCode = HttpStatus.OK.code; statusCode < HttpStatus.MULTIPLE_CHOICES.code; statusCode++) {
            testFunctionCalledForStatus(rangesBuilder(), successfulFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testOkFunctionCalledOn200() {
        testFunctionCalledForStatus(okFunction, HttpStatus.OK.code);
    }

    @Test
    public void testCreatedFunctionCalledOn201() {
        testFunctionCalledForStatus(createdFunction, HttpStatus.CREATED.code);
    }

    @Test
    public void testNoContentFunctionCalledOn204() {
        testFunctionCalledForStatus(noContentFunction, HttpStatus.NO_CONTENT.code);
    }


    @Test
    public void testInformationalFunctionCalledOn3xx() {
        for (int statusCode = HttpStatus.MULTIPLE_CHOICES.code; statusCode < HttpStatus.BAD_REQUEST.code; statusCode++) {
            testFunctionCalledForStatus(rangesBuilder(), redirectionFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testSeeOtherFunctionCalledOn303() {
        testFunctionCalledForStatus(seeOtherFunction, HttpStatus.SEE_OTHER.code);
    }

    @Test
    public void testSeeOtherFunctionCalledOn304() {
        testFunctionCalledForStatus(notModifiedFunction, HttpStatus.NOT_MODIFIED.code);
    }

    @Test
    public void testInformationalFunctionCalledOn4xx() {
        for (int statusCode = HttpStatus.BAD_REQUEST.code; statusCode < HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode++) {
            testFunctionCalledForStatus(rangesBuilder(), clientErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testBadRequestFunctionCalledOn400() {
        testFunctionCalledForStatus(badRequestFunction, HttpStatus.BAD_REQUEST.code);
    }

    @Test
    public void testBadRequestFunctionCalledOn401() {
        testFunctionCalledForStatus(unauthorizedFunction, HttpStatus.UNAUTHORIZED.code);
    }

    @Test
    public void testForbiddenFunctionCalledOn403() {
        testFunctionCalledForStatus(forbiddenFunction, HttpStatus.FORBIDDEN.code);
    }

    @Test
    public void testNotFoundFunctionCalledOn404() {
        testFunctionCalledForStatus(notFoundFunction, HttpStatus.NOT_FOUND.code);
    }

    @Test
    public void testConflictFunctionCalledOn409() {
        testFunctionCalledForStatus(conflictFunction, HttpStatus.CONFLICT.code);
    }

    @Test
    public void testInformationalFunctionCalledOn5xx() {
        for (int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.code; statusCode < 600; statusCode++) {
            testFunctionCalledForStatus(rangesBuilder(), serverErrorFunction, statusCode);
            resetAllMocks();
        }
    }

    @Test
    public void testInternalServerErrorFunctionCalledOn500() {
        testFunctionCalledForStatus(internalServerErrorFunction, HttpStatus.INTERNAL_SERVER_ERROR.code);
    }

    @Test
    public void testServiceUnavailableFunctionCalledOn503() {
        testFunctionCalledForStatus(serviceUnavailableFunction, HttpStatus.SERVICE_UNAVAILABLE.code);
    }

    @Test(expected = IllegalStateException.class)
    public void testFailThrowsException() {
        ResponseTransformation<Object> responseTransformation = newBuilder().fail(input -> {
            throw new IllegalStateException("foo");
        }).build();
        responseSettableFuture.completeExceptionally(new RuntimeException());
        Promise<Object> promise = toResponsePromise(forCompletionStage(responseSettableFuture)).transform(responseTransformation);
        promise.claim();
    }

    @Test
    public void testDelayedExecutionExecutesOnce() {
        final AtomicInteger counter = new AtomicInteger(0);
        when(this.response.getStatusCode()).thenReturn(200);

        ResponsePromise responsePromise = toResponsePromise(forCompletionStage(responseSettableFuture));

        ResponseTransformation<String> responseTransformation = DefaultResponseTransformation.<String>builder()
                .ok(input -> "foo" + counter.getAndIncrement())
                .fail(input -> {
                    throw new IllegalStateException();
                }).build();
        Promise<String> promise = responsePromise.transform(responseTransformation);
        responseSettableFuture.complete(this.response);
        Assert.assertEquals("foo0", promise.claim());
    }

    @Test
    public void testDelayedExecutionExecutesDoneOnceWithException() {
        final AtomicInteger counter = new AtomicInteger(0);

        when(this.response.getStatusCode()).thenReturn(200);

        ResponseTransformation<String> responseTransformation = DefaultResponseTransformation.<String>builder()
                .ok(input -> {
                    throw new IllegalArgumentException("foo" + counter.getAndIncrement());
                })
                .fail(input -> null).build();
        Promise<String> promise = toResponsePromise(forCompletionStage(responseSettableFuture)).transform(responseTransformation);

        responseSettableFuture.complete(this.response);
        try {
            promise.claim();
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("foo0", ex.getMessage());
        }
    }

    @Test
    public void testDelayedExecutionExecutesFailOnce() {
        final AtomicInteger counter = new AtomicInteger(0);
        when(this.response.getStatusCode()).thenReturn(200);

        ResponseTransformation<String> responseTransformation = DefaultResponseTransformation.<String>builder()
                .fail(input -> "foo" + counter.getAndIncrement())
                .ok(input -> {
                    throw new IllegalStateException();
                })
                .build();
        responseSettableFuture.complete(this.response);
        Promise<String> promise = toResponsePromise(forCompletionStage(responseSettableFuture)).transform(responseTransformation);
        Assert.assertEquals("foo0", promise.claim());
    }

    @Test
    public void testNotSuccessfulFunctionCalled() {


        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++) {
            final ResponseTransformation<Object> transformation = newBuilder()
                    .notSuccessful(clientErrorFunction)
                    .others(successfulFunction).build();
            if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code) {
                testFunctionCalledForStatus(transformation, successfulFunction, statusCode);
            } else {
                testFunctionCalledForStatus(transformation, clientErrorFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testErrorFunctionCalled() {

        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++) {
            final ResponseTransformation<Object> responseTransformation = newBuilder()
                    .error(clientErrorFunction)
                    .others(successfulFunction).build();
            if (HttpStatus.BAD_REQUEST.code <= statusCode) {
                testFunctionCalledForStatus(responseTransformation, clientErrorFunction, statusCode);
            } else {
                testFunctionCalledForStatus(responseTransformation, successfulFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testOthersFunctionCalled() {
        testFunctionCalledForStatus(
                newBuilder().others(othersFunction).build(),
                othersFunction,
                HttpStatus.OK.code);
    }

    @Test
    public void testFunctionAddedOnNonStandardHttpStatus() {
        newBuilder().on(601, input -> null);
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testFailCanThrowExceptions() {
        responseSettableFuture.completeExceptionally(new Throwable("Some message"));
        ResponsePromise responsePromise = toResponsePromise(forCompletionStage(responseSettableFuture));
        DefaultResponseTransformation.<String>builder()
                .ok(input -> "Ok")
                .fail(input -> {
                    throw new IllegalMonitorStateException();
                }).build().apply(responsePromise).claim();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testFailExceptionsWithoutHandlersAreThrownAtClaim() {
        responseSettableFuture.completeExceptionally(new IllegalMonitorStateException("Some message"));
        newBuilder().build().apply(toResponsePromise(forCompletionStage(responseSettableFuture))).claim();
    }

    private void testFunctionCalledForStatus(Function<Response, Object> function, int statusCode) {
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
                        .others(othersFunction).build(),
                function,
                statusCode);
    }

    private void testFunctionCalledForStatus(ResponseTransformation<Object> responseTransformation, Function<Response, Object> function, int statusCode) {
        when(response.getStatusCode()).thenReturn(statusCode);
        responseSettableFuture.complete(response);

        responseTransformation.apply(toResponsePromise(forCompletionStage(responseSettableFuture)));

        verify(function).apply(response);
        verifyNoMoreInteractions(allFunctionsAsArray());
    }

    private ResponseTransformation.Builder<Object> newBuilder() {
        return DefaultResponseTransformation.builder();
    }

    private ResponseTransformation<Object> rangesBuilder() {
        return newBuilder()
                .successful(successfulFunction)
                .informational(informationalFunction)
                .redirection(redirectionFunction)
                .clientError(clientErrorFunction)
                .serverError(serverErrorFunction)
                .others(othersFunction)
                .build();
    }

    private Object[] allFunctionsAsArray() {
        return toArray(allFunctions, Function.class);
    }

    private void resetAllMocks() {
        Mockito.reset(allFunctionsAsArray());
        responseSettableFuture = new CompletableFuture<>();
    }
}
