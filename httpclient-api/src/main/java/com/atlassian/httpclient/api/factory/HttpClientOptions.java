package com.atlassian.httpclient.api.factory;

import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Effects;

import java.util.concurrent.TimeUnit;

/**
 * Configuration options for the http client instance and its caching system
 */
public final class HttpClientOptions
{
    private String threadPrefix = "httpclient";
    private int ioThreadCount = 10;
    private long ioSelectInterval = 1000;

    private long connectionTimeout = 5 * 1000;
    private long socketTimeout = 20 * 1000;
    private long requestTimeout = 30 * 3000;

    private int maxConnectionsPerHost = 20;

    private long connectionPoolTimeToLive = 30 * 1000;

    private long maxCacheObjectSize = 100 * 1024L;
    private int maxCacheEntries = 100;

    private long maxEntitySize = 1024 * 1024 * 100;

    private Effect<Request> requestPreparer = Effects.noop();

    private SettableFutureHandlerFactory<Response> responseSettableFutureHandlerFactory
            = new SettableFutureHandlerFactory<Response>()
    {
        @Override
        public SettableFutureHandler<Response> create()
        {
            return new DefaultSettableFutureHandler<Response>();
        }
    };


    private String userAgent = "Atlassian HttpClient";

    /**
     * Determines the number of I/O dispatch threads to be used by the I/O reactor.
     * <p/>
     * Default: <code>10</code>
     */
    public int getIoThreadCount()
    {
        return ioThreadCount;
    }

    /**
     * @param ioThreadCount The number of I/O dispatch threads to be used by the I/O reactor.
     * May not be negative or zero.
     */
    public void setIoThreadCount(int ioThreadCount)
    {
        this.ioThreadCount = ioThreadCount;
    }

    /**
     * Determines time interval in milliseconds at which the I/O reactor wakes up to check for
     * timed out sessions and session requests.
     * <p/>
     * Default: <code>1000</code> milliseconds.
     */
    public long getIoSelectInterval()
    {
        return ioSelectInterval;
    }

    /**
     * Defines time interval in milliseconds at which the I/O reactor wakes up to check for
     * timed out sessions and session requests. May not be negative or zero.
     */
    public void setIoSelectInterval(int ioSelectInterval, TimeUnit timeUnit)
    {
        this.ioSelectInterval = timeUnit.toMillis(ioSelectInterval);
    }

    /**
     * @return How long, in milliseconds, to wait for a TCP connection
     */
    public long getConnectionTimeout()
    {
        return connectionTimeout;
    }

    /**
     * Sets how long, in milliseconds, to wait for a TCP connection
     * @param connectionTimeout Timeout value, defaults to 5000 milliseconds
     * @param timeUnit The time unit
     */
    public void setConnectionTimeout(int connectionTimeout, TimeUnit timeUnit)
    {
        this.connectionTimeout = timeUnit.toMillis(connectionTimeout);
    }

    /**
     * @return How long, in milliseconds, to wait for data over the socket
     */
    public long getSocketTimeout()
    {
        return socketTimeout;
    }

    /**
     * @param socketTimeout How long to wait for data, defaults to 20 seconds
     * @param timeUnit The time unit
     */
    public void setSocketTimeout(int socketTimeout, TimeUnit timeUnit)
    {
        this.socketTimeout = timeUnit.toMillis(socketTimeout);
    }

    /**
     * @return How long to wait for the entire request
     */
    public long getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * @param requestTimeout How long to wait for the entire request.  Defaults to 30 seconds.
     * @param timeUnit The time unit
     */
    public void setRequestTimeout(int requestTimeout, TimeUnit timeUnit)
    {
        this.requestTimeout = timeUnit.toMillis(requestTimeout);
    }

    /**
     * @return The user agent string
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * @param userAgent The user agent string
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * @return Name prefix to use for spawned threads
     */
    public String getThreadPrefix()
    {
        return threadPrefix;
    }

    /**
     * @param threadPrefix Name prefix to use for spawned threads
     */
    public void setThreadPrefix(String threadPrefix)
    {
        this.threadPrefix = threadPrefix;
    }

    /**
     * @return How long, in milliseconds, to allow connections to live in the pool.  Defaults
     * to 30 seconds.
     */
    public long getConnectionPoolTimeToLive()
    {
        return connectionPoolTimeToLive;
    }

    /**
     * @param connectionPoolTimeToLive How long to allow connections to live in the pool
     * @param timeUnit The time unit
     */
    public void setConnectionPoolTimeToLive(int connectionPoolTimeToLive, TimeUnit timeUnit)
    {
        this.connectionPoolTimeToLive = timeUnit.toMillis(connectionPoolTimeToLive);
    }

    /**
     * @return How many simultaneous connections are allowed per host.  Defaults to 20
     */
    public int getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * @param maxConnectionsPerHost How many connections are allowed per host
     */
    public void setMaxConnectionsPerHost(int maxConnectionsPerHost)
    {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * @return The max object size, in bytes, allowed in the HTTP cache.  Defaults to 100k
     */
    public long getMaxCacheObjectSize()
    {
        return maxCacheObjectSize;
    }

    /**
     * @param maxCacheObjectSize The max cache object size in bytes
     */
    public void setMaxCacheObjectSize(long maxCacheObjectSize)
    {
        this.maxCacheObjectSize = maxCacheObjectSize;
    }

    /**
     * @return The max cache entries.  Defaults to 1000.
     */
    public int getMaxCacheEntries()
    {
        return maxCacheEntries;
    }

    /**
     * @param maxCacheEntries The max cache entries
     */
    public void setMaxCacheEntries(int maxCacheEntries)
    {
        this.maxCacheEntries = maxCacheEntries;
    }

    /**
     * @return The factory for creating the object responsible for managing settable futures
     */
    public SettableFutureHandlerFactory<Response> getResponseSettableFutureHandlerFactory()
    {
        return responseSettableFutureHandlerFactory;
    }

    /**
     * @param responseSettableFutureHandlerFactory The {@link SettableFutureHandler} instance that
     *                                             will manage the creation of the response
     *                                             future
     */
    public void setResponseSettableFutureHandlerFactory(
            SettableFutureHandlerFactory<Response> responseSettableFutureHandlerFactory)
    {
        this.responseSettableFutureHandlerFactory = responseSettableFutureHandlerFactory;
    }

    /**
     * @return The effect to apply before the request is executed
     */
    public Effect<Request> getRequestPreparer()
    {
        return requestPreparer;
    }

    /**
     * @param requestPreparer The effect to apply before the request is executed
     */
    public void setRequestPreparer(Effect<Request> requestPreparer)
    {
        this.requestPreparer = requestPreparer;
    }

    /**
     * @return The maximum entity size in bytes.  Default is 100MB
     */
    public long getMaxEntitySize()
    {
        return maxEntitySize;
    }

    /**
     * param maxEntitySize The maximum entity size in bytes
     */
    public void setMaxEntitySize(long maxEntitySize)
    {
        this.maxEntitySize = maxEntitySize;
    }
}
