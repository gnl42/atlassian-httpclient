package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.junit.http.jetty.JettyServer;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultHttpClientIntegrationTest {
    private static final ThreadLocal<Object> TEST_THREAD_LOCAL = new ThreadLocal<>();

    private static final AtomicBoolean NO_OP_THREAD_LOCAL_CONTEXT_MANAGER = new AtomicBoolean(false);

    @ClassRule
    public static final JettyServer SERVER = new JettyServer();

    private ApacheAsyncHttpClient httpClient;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() {
        NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.set(false); //By default use thread manager in all tests

        when(applicationProperties.getDisplayName()).thenReturn(DefaultHttpClientIntegrationTest.class.getSimpleName());
        when(applicationProperties.getVersion()).thenReturn("1");
        when(applicationProperties.getBuildNumber()).thenReturn("0001");

        httpClient = new ApacheAsyncHttpClient<>(eventPublisher, applicationProperties,
                new ThreadLocalContextManager<Object>() {
                    @Override
                    public Object getThreadLocalContext() {
                        return NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get() ? null : TEST_THREAD_LOCAL.get();
                    }

                    @Override
                    public void setThreadLocalContext(Object context) {
                        if (!NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get()) {
                            TEST_THREAD_LOCAL.set(context);
                        }
                    }

                    @Override
                    public void clearThreadLocalContext() {
                        if (!NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.get()) {
                            TEST_THREAD_LOCAL.set(null);
                        }
                    }
                });
    }

    @Test
    public void threadLocalVariablesAreAvailableToFunctionsWithWorkingThreadLocalContextManager() {
        final AtomicBoolean okFunctionCalled = new AtomicBoolean(false);
        final long testThreadId = Thread.currentThread().getId();

        final Object objectInThreadLocal = new Object();
        TEST_THREAD_LOCAL.set(objectInThreadLocal);

        ResponsePromise responsePromise = httpClient.newRequest(SERVER.newUri("/test")).get();
        final Object claimedObject = httpClient.transformation().ok(response -> {
            okFunctionCalled.set(true);
            assertTrue("For this test to work the function should be executed in a separate thread!", testThreadId != Thread.currentThread().getId());
            assertTrue(Thread.currentThread().getName().startsWith("httpclient-callbacks"));
            return TEST_THREAD_LOCAL.get();
        }).build().apply(responsePromise).claim();

        assertTrue(okFunctionCalled.get());
        assertSame(objectInThreadLocal, claimedObject);
    }

    @Test
    public void threadLocalVariablesAreNotAvailableToFunctionsWithNoOpThreadLocalContextManager() {
        NO_OP_THREAD_LOCAL_CONTEXT_MANAGER.set(true);

        final AtomicBoolean okFunctionCalled = new AtomicBoolean(false);
        final long testThreadId = Thread.currentThread().getId();

        final Object objectInThreadLocal = new Object();
        TEST_THREAD_LOCAL.set(objectInThreadLocal);

        ResponsePromise responsePromise = httpClient.newRequest(SERVER.newUri("/test")).get();
        final Object claimedObject = httpClient.transformation().ok(response -> {
            okFunctionCalled.set(true);
            assertTrue("For this test to work the function should be executed in a separate thread!", testThreadId != Thread.currentThread().getId());
            assertTrue(Thread.currentThread().getName().startsWith("httpclient-callbacks"));
            return TEST_THREAD_LOCAL.get();
        }).build().apply(responsePromise).claim();

        assertTrue(okFunctionCalled.get());
        assertNull(claimedObject);
    }

    @Test
    public void contextClassLoaderSetForCallback() {
        ClassLoader tmpClassLoader = new URLClassLoader(new URL[0]);
        Thread.currentThread().setContextClassLoader(tmpClassLoader);

        ResponsePromise responsePromise = httpClient.newRequest(SERVER.newUri("/test")).get();
        ClassLoader callbackClassLoader = httpClient.<ClassLoader>transformation()
                .ok(response -> Thread.currentThread().getContextClassLoader())
                .build()
                .apply(responsePromise)
                .claim();
        assertEquals(tmpClassLoader, callbackClassLoader);
    }
}
