package com.atlassian.httpclient.api;

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

import static com.google.common.collect.Iterables.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class ResponsePromiseTransformationBuilderTest
{
    private ResponsePromiseTransformationBuilder<Object> builder;

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

    @Test
    public void testNotSuccessfulFunctionCalled()
    {
        final ResponsePromiseTransformationBuilder<Object> builder = newBuilder()
                .notSuccessful(clientErrorFunction)
                .others(successfulFunction);

        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++)
        {
            if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code)
            {
                testFunctionCalledForStatus(builder, successfulFunction, statusCode);
            }
            else
            {
                testFunctionCalledForStatus(builder, clientErrorFunction, statusCode);
            }
            resetAllMocks();
        }
    }

    @Test
    public void testErrorFunctionCalled()
    {
        final ResponsePromiseTransformationBuilder<Object> builder = newBuilder()
                .error(clientErrorFunction)
                .others(successfulFunction);

        for (int statusCode = HttpStatus.CONTINUE.code; statusCode < 600; statusCode++)
        {
            if (HttpStatus.BAD_REQUEST.code <= statusCode && statusCode < 600)
            {
                testFunctionCalledForStatus(builder, clientErrorFunction, statusCode);
            }
            else
            {
                testFunctionCalledForStatus(builder, successfulFunction, statusCode);
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

    private void testFunctionCalledForStatus(ResponsePromiseTransformationBuilder<Object> builder, Function<Response, Object> function, int statusCode)
    {
        when(response.getStatusCode()).thenReturn(statusCode);

        builder.toPromise();
        responseSettableFuture.set(response);

        verify(function).apply(response);
        verifyNoMoreInteractions(allFunctionsAsArray());
    }

    private ResponsePromiseTransformationBuilder<Object> newBuilder()
    {
        return new ResponsePromiseTransformationBuilder<Object>(ResponsePromises.toResponsePromise(responseSettableFuture));
    }

    private ResponsePromiseTransformationBuilder<Object> rangesBuilder()
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
    }
}
