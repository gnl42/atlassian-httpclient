package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.junit.http.jetty.JettyServer;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public final class DefaultHttpClientIntegrationTest
{
    private static final ThreadLocal<Object> TEST_THREAD_LOCAL = new ThreadLocal<Object>();

    private static final AtomicBoolean NO_OP_THREAD_LOCAL_CONTEXT_MANAGER = new AtomicBoolean(false);

    @ClassRule
    public static final JettyServer SERVER = new JettyServer();

    private HttpClient httpClient;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp()
    {
        when(applicationProperties.getDisplayName()).thenReturn(DefaultHttpClientIntegrationTest.class.getSimpleName());
        when(applicationProperties.getVersion()).thenReturn("1");
        when(applicationProperties.getBuildNumber()).thenReturn("0001");

        httpClient = new DefaultHttpClient<Object>(eventPublisher, applicationProperties, new ThreadLocalContextManager<Object>()
        {
            @Override
            public Object getThreadLocalContext()
            {
                return NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get() ? null : TEST_THREAD_LOCAL.get();
            }

            @Override
            public void setThreadLocalContext(Object context)
            {
                if (!NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get())
                {
                    TEST_THREAD_LOCAL.set(context);
                }
            }

            @Override
            public void clearThreadLocalContext()
            {
                if (!NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get())
                {
                    TEST_THREAD_LOCAL.set(null);
                }
            }
        });
    }

    @Test
    public void threadLocalVariablesAreAvailableToFunctionsWithWorkingThreadLocalContextManager()
    {
        final AtomicBoolean okFunctionCalled = new AtomicBoolean(false);
        final long testThreadId = Thread.currentThread().getId();

        final Object objectInThreadLocal = new Object();
        TEST_THREAD_LOCAL.set(objectInThreadLocal);

        final Object claimedObject = httpClient.builders().transform().ok(new Function<Response, Object>()
        {
            @Override
            public Object apply(Response response)
            {
                okFunctionCalled.set(true);
                assertTrue("For this test to work the function should be executed in a separate thread!", testThreadId != Thread.currentThread().getId());
                assertTrue(Thread.currentThread().getName().startsWith("httpclient-callbacks"));
                return TEST_THREAD_LOCAL.get();
            }
        }).build().transform(httpClient.execute(httpClient.newRequest().get().url(SERVER.newUri("/test")).build())).claim();

        assertTrue(okFunctionCalled.get());
        assertSame(objectInThreadLocal, claimedObject);
    }

    @Test
    public void threadLocalVariablesAreNotAvailableToFunctionsWithNoOpThreadLocalContextManager()
    {
        NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.set(true);

        final AtomicBoolean okFunctionCalled = new AtomicBoolean(false);
        final long testThreadId = Thread.currentThread().getId();

        final Object objectInThreadLocal = new Object();
        TEST_THREAD_LOCAL.set(objectInThreadLocal);

        final Request request = httpClient.newRequest(SERVER.newUri("/test")).get().build();
        final Object claimedObject = httpClient.execute(request).map(new Function<Response, Object>()
        {
            @Override
            public Object apply(final Response response)
            {
                okFunctionCalled.set(true);
                assertTrue("For this test to work the function should be executed in a separate thread!", testThreadId != Thread.currentThread().getId());
                assertTrue(Thread.currentThread().getName().startsWith("httpclient-callbacks"));
                return TEST_THREAD_LOCAL.get();
            }
        }).claim();

        assertTrue(okFunctionCalled.get());
        assertNull(claimedObject);
    }

    @Test
    public void contextClassLoaderSetForCallback()
    {
        ClassLoader tmpClassLoader = new URLClassLoader(new URL[0]);
        Thread.currentThread().setContextClassLoader(tmpClassLoader);

        Request request = httpClient.newRequest(SERVER.newUri("/test")).get().build();
        ClassLoader callbackClassLoader = httpClient.execute(request).map(new Function<Response, ClassLoader>()
        {
            @Override
            public ClassLoader apply(final Response response)
            {
                return Thread.currentThread().getContextClassLoader();
            }
        }).claim();

        assertEquals(tmpClassLoader, callbackClassLoader);
    }
}
