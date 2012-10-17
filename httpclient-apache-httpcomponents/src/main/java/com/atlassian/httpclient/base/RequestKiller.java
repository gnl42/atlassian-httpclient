package com.atlassian.httpclient.base;

import org.apache.http.client.methods.AbortableHttpRequest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Sets.*;

/**
 * A request killer for ensuring a request doesn't take too long.  While connection and socket
 * timeouts deal with waiting for an TCP connection and a long delay in new content, respectively,
 * this thread kills requests that may be trickling content down in such a way that doesn't trip
 * the socket timeout.
 */
public final class RequestKiller implements Runnable
{
    private final Set<RequestEntry> activeRequests = new CopyOnWriteArraySet<RequestEntry>();
    private final Thread killerThread;

    public RequestKiller(String namePrefix)
    {
        killerThread = new Thread(this, namePrefix + "-req-killer");
    }

    public void registerRequest(AbortableHttpRequest request, long millisToLive)
    {
        RequestEntry entry = new RequestEntry(request, millisToLive);
        activeRequests.add(entry);
    }

    public void start()
    {
        killerThread.start();
    }

    public void stop() throws Exception
    {
        killerThread.interrupt();
    }

    @Override
    public void run()
    {
        while (true)
        {
            Set<RequestEntry> entriesToRemove = newHashSet();
            long now = System.currentTimeMillis();
            for (RequestEntry entry : activeRequests)
            {
                if (now > entry.getExpiry())
                {
                    entry.abort();
                    entriesToRemove.add(entry);
                }
            }
            activeRequests.removeAll(entriesToRemove);
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                break;
                // it's cool, in shutdown
            }
        }
    }

    public void completedRequest(AbortableHttpRequest request)
    {
        activeRequests.remove(new RequestEntry(request));
    }

    private static final class RequestEntry
    {
        private final AbortableHttpRequest request;
        private final long expiry;

        // used for deregistration
        private RequestEntry(AbortableHttpRequest request)
        {
            this.request = request;
            this.expiry = 0;
        }

        private RequestEntry(AbortableHttpRequest request, long millisToLive)
        {
            this.request = request;
            this.expiry = System.currentTimeMillis() + millisToLive;
        }

        public void abort()
        {
            request.abort();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            RequestEntry that = (RequestEntry) o;

            if (!request.equals(that.request))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return request.hashCode();
        }

        public long getExpiry()
        {
            return expiry;
        }
    }
}
