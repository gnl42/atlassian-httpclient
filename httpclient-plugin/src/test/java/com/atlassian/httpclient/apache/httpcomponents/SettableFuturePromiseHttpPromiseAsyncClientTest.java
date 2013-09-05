package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.httpclient.apache.httpcomponents.DefaultAsyncHttpClient.runInContext;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public final class SettableFuturePromiseHttpPromiseAsyncClientTest
{
    @Mock
    private ThreadLocalContextManager<Object> threadLocalContextManager;

    @Test
    public void testRunInContext()
    {
        final AtomicBoolean run = new AtomicBoolean(false);

        final Object currentThreadLocalContext = new Object();
        when(threadLocalContextManager.getThreadLocalContext()).thenReturn(currentThreadLocalContext);

        final Object newThreadLocalContext = new Object();
        try
        {
            runInContext(threadLocalContextManager, newThreadLocalContext, this.getClass().getClassLoader(), new Runnable()
            {
                @Override
                public void run()
                {
                    run.set(true);
                    throw new RuntimeException(); // this shouldn't affect things
                }
            });
        }
        catch (Exception e)
        {
            // ignore
        }

        assertTrue(run.get());


        final InOrder inOrder = inOrder(threadLocalContextManager);

        inOrder.verify(threadLocalContextManager).getThreadLocalContext();
        inOrder.verify(threadLocalContextManager).setThreadLocalContext(newThreadLocalContext);
        inOrder.verify(threadLocalContextManager).setThreadLocalContext(currentThreadLocalContext);

        // never reset
        verify(threadLocalContextManager, never()).clearThreadLocalContext();
    }
}
