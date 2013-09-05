package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

final class DefaultAsyncHttpClient<C> implements AsyncHttpClient
{
    private final HttpAsyncClient client;
    private final ThreadLocalContextManager<C> threadLocalContextManager;

    DefaultAsyncHttpClient(HttpAsyncClient client, ThreadLocalContextManager<C> threadLocalContextManager)
    {
        this.client = checkNotNull(client);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
    }

    @Override
    public Promise<HttpResponse> execute(HttpUriRequest request, HttpContext context)
    {
        final SettableFuture<HttpResponse> future = SettableFuture.create();
        client.execute(request, context, new ThreadLocalContextAwareFutureCallback<C, HttpResponse>(threadLocalContextManager)
        {
            @Override
            void doCompleted(final HttpResponse httpResponse)
            {
                future.set(httpResponse);
            }

            @Override
            void doFailed(final Exception ex)
            {
                future.setException(ex);
            }

            @Override
            void doCancelled()
            {
                final TimeoutException timeoutException = new TimeoutException();
                future.setException(timeoutException);
            }
        });
        return Promises.forListenableFuture(future);
    }

    @VisibleForTesting
    private static <C> void runInContext(ThreadLocalContextManager<C> threadLocalContextManager, C threadLocalContext,
            ClassLoader contextClassLoader, Runnable runnable)
    {
        final C oldThreadLocalContext = threadLocalContextManager.getThreadLocalContext();
        final ClassLoader oldCcl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            threadLocalContextManager.setThreadLocalContext(threadLocalContext);
            runnable.run();
        }
        finally
        {
            threadLocalContextManager.setThreadLocalContext(oldThreadLocalContext);
            Thread.currentThread().setContextClassLoader(oldCcl);
        }
    }

    private static abstract class ThreadLocalContextAwareFutureCallback<C, R> implements FutureCallback<R>
    {
        private final ThreadLocalContextManager<C> threadLocalContextManager;
        private final C threadLocalContext;
        private final ClassLoader contextClassLoader;

        private ThreadLocalContextAwareFutureCallback(ThreadLocalContextManager<C> threadLocalContextManager)
        {
            this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
            this.threadLocalContext = threadLocalContextManager.getThreadLocalContext();
            this.contextClassLoader = Thread.currentThread().getContextClassLoader();
        }

        abstract void doCompleted(R response);

        abstract void doFailed(Exception ex);

        abstract void doCancelled();

        @Override
        public final void completed(final R response)
        {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, new Runnable()
            {
                @Override
                public void run()
                {
                    doCompleted(response);
                }
            });
        }

        @Override
        public final void failed(final Exception ex)
        {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, new Runnable()
            {
                @Override
                public void run()
                {
                    doFailed(ex);
                }
            });
        }

        @Override
        public final void cancelled()
        {
            runInContext(threadLocalContextManager, threadLocalContext, contextClassLoader, new Runnable()
            {
                @Override
                public void run()
                {
                    doCancelled();
                }
            });
        }
    }
}
