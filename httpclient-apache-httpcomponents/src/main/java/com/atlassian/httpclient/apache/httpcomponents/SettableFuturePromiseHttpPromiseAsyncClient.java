package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.*;

final class SettableFuturePromiseHttpPromiseAsyncClient<C> implements PromiseHttpAsyncClient
{
    private final HttpAsyncClient client;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final Executor executor;

    SettableFuturePromiseHttpPromiseAsyncClient(HttpAsyncClient client, ThreadLocalContextManager<C> threadLocalContextManager, Executor executor)
    {
        this.client = checkNotNull(client);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        this.executor = new ThreadLocalDelegateExecutor<C>(threadLocalContextManager, executor);
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
                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        future.set(httpResponse);
                    }
                });
            }

            @Override
            void doFailed(final Exception ex)
            {
                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        future.setException(ex);
                    }
                });
            }

            @Override
            void doCancelled()
            {
                final TimeoutException timeoutException = new TimeoutException();
                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        future.setException(timeoutException);
                    }
                });
            }
        });
        return Promises.forListenableFuture(future);
    }

    private static abstract class ThreadLocalContextAwareFutureCallback<C, HttpResponse> implements FutureCallback<HttpResponse>
    {
        private final ThreadLocalContextManager<C> threadLocalContextManager;
        private final C threadLocalContext;

        private ThreadLocalContextAwareFutureCallback(ThreadLocalContextManager<C> threadLocalContextManager)
        {
            this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
            this.threadLocalContext = threadLocalContextManager.getThreadLocalContext();
        }

        abstract void doCompleted(HttpResponse response);

        abstract void doFailed(Exception ex);

        abstract void doCancelled();

        @Override
        public final void completed(HttpResponse response)
        {
            try
            {
                threadLocalContextManager.setThreadLocalContext(threadLocalContext);
                doCompleted(response);
            }
            finally
            {
                threadLocalContextManager.resetThreadLocalContext();
            }
        }

        @Override
        public final void failed(Exception ex)
        {
            try
            {
                threadLocalContextManager.setThreadLocalContext(threadLocalContext);
                doFailed(ex);
            }
            finally
            {
                threadLocalContextManager.resetThreadLocalContext();
            }
        }

        @Override
        public final void cancelled()
        {
            try
            {
                threadLocalContextManager.setThreadLocalContext(threadLocalContext);
                doCancelled();
            }
            finally
            {
                threadLocalContextManager.resetThreadLocalContext();
            }
        }
    }

    private static final class ThreadLocalDelegateExecutor<C> implements Executor
    {
        private final Executor delegate;
        private final ThreadLocalContextManager<C> manager;

        ThreadLocalDelegateExecutor(ThreadLocalContextManager<C> manager, Executor delegate)
        {
            this.delegate = checkNotNull(delegate);
            this.manager = checkNotNull(manager);
        }

        public void execute(Runnable runnable)
        {
            delegate.execute(new ThreadLocalDelegateRunnable<C>(manager, runnable));
        }
    }

    private static final class ThreadLocalDelegateRunnable<C> implements Runnable
    {
        private final C context;
        private final Runnable delegate;
        private final ThreadLocalContextManager<C> manager;

        ThreadLocalDelegateRunnable(ThreadLocalContextManager<C> manager, Runnable delegate)
        {
            this.delegate = delegate;
            this.manager = manager;
            this.context = manager.getThreadLocalContext();
        }

        public void run()
        {
            try
            {
                manager.setThreadLocalContext(context);
                delegate.run();
            }
            finally
            {
                manager.resetThreadLocalContext();
            }
        }
    }
}
