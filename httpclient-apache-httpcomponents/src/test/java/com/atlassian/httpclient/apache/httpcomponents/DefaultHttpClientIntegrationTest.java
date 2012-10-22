package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.junit.http.jetty.JettyServer;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultHttpClientIntegrationTest
{
    private static final ThreadLocal<Object> TEST_THREAD_LOCAL = new ThreadLocal<Object>();

    private static final AtomicBoolean NO_OP_THREAD_LOCAL_CONTEXT_MANAGER = new AtomicBoolean(false);

    @ClassRule
    public static final JettyServer SERVER = new JettyServer();

    private DefaultHttpClient httpClient;

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
            public void resetThreadLocalContext()
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

        final Object claimedObject = httpClient.newRequest(SERVER.newUri("/test")).get().transform().ok(new Function<Response, Object>()
        {
            @Override
            public Object apply(Response response)
            {
                okFunctionCalled.set(true);
                assertTrue("For this test to work the function should be executed in a separate thread!", testThreadId != Thread.currentThread().getId());
                assertTrue(Thread.currentThread().getName().startsWith("httpclient-callbacks"));
                return TEST_THREAD_LOCAL.get();
            }
        }).claim();

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

        final Object claimedObject = httpClient.newRequest(SERVER.newUri("/test")).get().transform().ok(new Function<Response, Object>()
        {
            @Override
            public Object apply(Response response)
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
}
