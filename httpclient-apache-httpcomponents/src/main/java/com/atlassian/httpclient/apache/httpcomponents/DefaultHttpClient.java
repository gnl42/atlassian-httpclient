package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.*;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.atlassian.httpclient.base.RequestKiller;
import com.atlassian.httpclient.api.factory.SettableFutureHandler;
import com.atlassian.httpclient.base.event.HttpRequestCompletedEvent;
import com.atlassian.httpclient.base.event.HttpRequestFailedEvent;
import com.atlassian.util.concurrent.ThreadFactories;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class DefaultHttpClient extends AbstractHttpClient implements HttpClient, DisposableBean
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final RequestKiller requestKiller;
    private final EventPublisher eventPublisher;
    private final HttpAsyncClient httpClient;
    private final HttpAsyncClient nonCachingHttpClient;

    private final HttpClientOptions httpClientOptions;
    private final FlushableHttpCacheStorage httpCacheStorage;

    public DefaultHttpClient(EventPublisher eventPublisher)
    {
        this(eventPublisher, new HttpClientOptions());
    }

    public DefaultHttpClient(EventPublisher eventPublisher, HttpClientOptions options)
    {
        this.requestKiller = new RequestKiller(options.getThreadPrefix());
        this.eventPublisher = eventPublisher;
        this.httpClientOptions = options;

        DefaultHttpAsyncClient client;
        try
        {
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(options.getIoThreadCount());
            ioReactorConfig.setSelectInterval(options.getIoSelectInterval());
            ioReactorConfig.setInterestOpQueued(true);
            DefaultConnectingIOReactor reactor = new DefaultConnectingIOReactor(
                    ioReactorConfig,
                    ThreadFactories.namedThreadFactory(options.getThreadPrefix() + "-io",
                            ThreadFactories.Type.DAEMON));
            reactor.setExceptionHandler(new IOReactorExceptionHandler()
            {
                @Override
                public boolean handle(IOException ex)
                {
                    log.error("IO exception in reactor", ex);
                    return false;
                }

                @Override
                public boolean handle(RuntimeException ex)
                {
                    log.error("Fatal runtime error", ex);
                    return false;
                }
            });
            final PoolingClientAsyncConnectionManager connmgr = new PoolingClientAsyncConnectionManager(reactor,
                    AsyncSchemeRegistryFactory.createDefault(), options.getConnectionPoolTimeToLive(), TimeUnit.MILLISECONDS)
            {
                @Override
                protected void finalize() throws Throwable
                {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            };

            connmgr.setDefaultMaxPerRoute(options.getMaxConnectionsPerHost());

            client = new DefaultHttpAsyncClient(connmgr);
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor " + options.getThreadPrefix() + "not set up correctly", e);
        }

        HttpParams params = client.getParams();
        // @todo add plugin version to UA string
        HttpProtocolParams.setUserAgent(params, options.getUserAgent());

        HttpConnectionParams.setConnectionTimeout(params, (int) options.getConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, (int) options.getSocketTimeout());
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setTcpNoDelay(params, true);

        ProxySelectorAsyncRoutePlanner routePlanner = new ProxySelectorAsyncRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(options.getMaxCacheEntries());
        cacheConfig.setSharedCache(false);
        cacheConfig.setMaxObjectSize(options.getMaxCacheObjectSize());
        cacheConfig.setNeverCache1_0ResponsesWithQueryString(false);

        this.nonCachingHttpClient = client;
        this.httpCacheStorage = new FlushableHttpCacheStorage(cacheConfig);
        httpClient = new CachingHttpAsyncClient(client, httpCacheStorage, cacheConfig);

        httpClient.start();
        requestKiller.start();
    }

    public ResponsePromise execute(final DefaultRequest request)
    {
        httpClientOptions.getRequestPreparer().apply(request);

        // validate the request state
        request.validate();

        // freeze the request state to prevent further mutability as we go to execute the request
        request.freeze();

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        DefaultRequest.Method method = request.getMethodEnum();
        switch (method)
        {
            case GET:
                op = new HttpGet(uri);
                break;
            case POST:
                op = new HttpPost(uri);
                break;
            case PUT:
                op = new HttpPut(uri);
                break;
            case DELETE:
                op = new HttpDelete(uri);
                break;
            case OPTIONS:
                op = new HttpOptions(uri);
                break;
            case HEAD:
                op = new HttpHead(uri);
                break;
            case TRACE:
                op = new HttpTrace(uri);
                break;
            default:
                throw new UnsupportedOperationException(method.toString());
        }
        if (request.hasEntity())
        {
            if (op instanceof HttpEntityEnclosingRequestBase)
            {
                ((HttpEntityEnclosingRequestBase) op).setEntity(request.getHttpEntity());
            }
            else
            {
                throw new UnsupportedOperationException("HTTP method " + method + " does not support sending an entity");
            }
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet())
        {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        HttpContext localContext = new BasicHttpContext();
        final SettableFutureHandler<Response> future = httpClientOptions.getResponseSettableFutureHandlerFactory().create();
        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse httpResponse)
            {
                requestKiller.completedRequest(op);
                long elapsed = System.currentTimeMillis() - start;
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300)
                {
                    eventPublisher.publish(new HttpRequestCompletedEvent(uri, statusCode, elapsed, request.getAttributes()));
                }
                else
                {
                    eventPublisher.publish(new HttpRequestFailedEvent(uri, statusCode, elapsed, request.getAttributes()));
                }
                try
                {
                    DefaultResponse response = translate(httpResponse);
                    response.freeze();
                    future.set(response);
                }
                catch (IOException ex)
                {
                    this.failed(ex);
                }
            }

            @Override
            public void failed(Exception ex)
            {
                requestKiller.completedRequest(op);
                long elapsed = System.currentTimeMillis() - start;
                eventPublisher.publish(new HttpRequestFailedEvent(uri, ex.toString(), elapsed, request.getAttributes()));
                future.setException(ex);
            }

            @Override
            public void cancelled()
            {
                requestKiller.completedRequest(op);
                TimeoutException ex = new TimeoutException();
                long elapsed = System.currentTimeMillis() - start;
                eventPublisher.publish(new HttpRequestFailedEvent(uri, ex.toString(), elapsed, request.getAttributes()));
                future.setException(ex);
            }
        };

        requestKiller.registerRequest(op, httpClientOptions.getRequestTimeout());
        HttpAsyncClient actualClient = request.isCacheDisabled() ? nonCachingHttpClient : httpClient;
        actualClient.execute(op, localContext, futureCallback);
        return ResponsePromises.toResponsePromise(future.getFuture());
    }

    @Override
    public void destroy() throws Exception
    {
        requestKiller.stop();
        httpClient.getConnectionManager().shutdown();
    }

    private DefaultResponse translate(HttpResponse httpResponse)
            throws IOException
    {
        StatusLine status = httpResponse.getStatusLine();
        DefaultResponse response = new DefaultResponse(httpClientOptions.getMaxEntitySize());
        response.setStatusCode(status.getStatusCode());
        response.setStatusText(status.getReasonPhrase());
        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders)
        {
            response.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            response.setEntityStream(entity.getContent());
        }
        return response;
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpCacheStorage.flushByUriPattern(urlPattern);
    }
}
