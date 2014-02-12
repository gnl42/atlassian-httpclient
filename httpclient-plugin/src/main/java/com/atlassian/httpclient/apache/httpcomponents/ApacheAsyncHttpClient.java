package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorage;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorageImpl;
import com.atlassian.httpclient.apache.httpcomponents.cache.LoggingHttpCacheStorage;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.atlassian.httpclient.base.event.HttpRequestCompletedEvent;
import com.atlassian.httpclient.base.event.HttpRequestFailedEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.conn.ssl.SSLLayeringStrategy;
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
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.atlassian.util.concurrent.Promises.rejected;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class ApacheAsyncHttpClient<C> extends AbstractHttpClient implements HttpClient, DisposableBean
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Supplier<String> httpClientVersion = Suppliers.memoize(new Supplier<String>()
    {
        @Override
        public String get()
        {
            return MavenUtils.getVersion("com.atlassian.httpclient", "atlassian-httpclient-api");
        }
    });

    private final Function<Object, Void> eventConsumer;
    private final Supplier<String> applicationName;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final ExecutorService callbackExecutor;
    private final HttpClientOptions httpClientOptions;

    private final HttpAsyncClient httpClient;
    private final HttpAsyncClient nonCachingHttpClient;
    private final FlushableHttpCacheStorage httpCacheStorage;

    public ApacheAsyncHttpClient(EventPublisher eventConsumer, ApplicationProperties applicationProperties, ThreadLocalContextManager<C> threadLocalContextManager)
    {
        this(eventConsumer, applicationProperties, threadLocalContextManager, new HttpClientOptions());
    }

    public ApacheAsyncHttpClient(final EventPublisher eventConsumer, final ApplicationProperties applicationProperties, ThreadLocalContextManager<C> threadLocalContextManager, final HttpClientOptions options)
    {
        this(new DefaultApplicationNameSupplier(applicationProperties),
                new EventConsumerFunction(eventConsumer),
                threadLocalContextManager,
                options);
    }

    public ApacheAsyncHttpClient(String applicationName)
    {
        this(applicationName, new HttpClientOptions());
    }

    public ApacheAsyncHttpClient(String applicationName, final HttpClientOptions options)
    {
        this(Suppliers.ofInstance(applicationName), Functions.constant((Void) null), new NoOpThreadLocalContextManager<C>(), options);
    }

    public ApacheAsyncHttpClient(Supplier<String> applicationName, Function<Object, Void> eventConsumer, ThreadLocalContextManager<C> threadLocalContextManager, final HttpClientOptions options)
    {
        this.eventConsumer = checkNotNull(eventConsumer);
        this.applicationName = checkNotNull(applicationName);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        this.httpClientOptions = checkNotNull(options);

        final DefaultHttpAsyncClient client;
        try
        {
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(options.getIoThreadCount());
            ioReactorConfig.setSelectInterval(options.getIoSelectInterval());
            ioReactorConfig.setInterestOpQueued(true);
            DefaultConnectingIOReactor reactor = new DefaultConnectingIOReactor(
                    ioReactorConfig,
                    ThreadFactories.namedThreadFactory(options.getThreadPrefix() + "-io", ThreadFactories.Type.DAEMON));
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
                    getAsyncSchemeRegistryFactory(options.trustSelfSignedCertificates()), options.getConnectionPoolTimeToLive(), options.getLeaseTimeout(), TimeUnit.MILLISECONDS)
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

            client.setRoutePlanner(new ProxyRoutePlanner(connmgr.getSchemeRegistry()));
            HttpClientProxyConfig.applyProxyCredentials(client, connmgr.getSchemeRegistry());

            client.setRedirectStrategy(new DefaultRedirectStrategy()
            {
                final String[] REDIRECT_METHODS = {HttpHead.METHOD_NAME, HttpGet.METHOD_NAME, HttpPost.METHOD_NAME, HttpPut.METHOD_NAME, HttpDelete.METHOD_NAME, HttpPatch.METHOD_NAME};

                @Override
                protected boolean isRedirectable(String method)
                {
                    for (String m : REDIRECT_METHODS)
                    {
                        if (m.equalsIgnoreCase(method))
                        {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException
                {
                    URI uri = getLocationURI(request, response, context);
                    String method = request.getRequestLine().getMethod();
                    if (method.equalsIgnoreCase(HttpHead.METHOD_NAME))
                    {
                        return new HttpHead(uri);
                    }
                    else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME))
                    {
                        return new HttpGet(uri);
                    }
                    else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME))
                    {
                        final HttpPost post = new HttpPost(uri);
                        if (request instanceof HttpEntityEnclosingRequest)
                        {
                            post.setEntity(((HttpEntityEnclosingRequest) request).getEntity());
                        }
                        return post;
                    }
                    else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME))
                    {
                        return new HttpPut(uri);
                    }
                    else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME))
                    {
                        return new HttpDelete(uri);
                    }
                    else if (method.equalsIgnoreCase(HttpPatch.METHOD_NAME))
                    {
                        return new HttpPatch(uri);
                    }
                    else
                    {
                        return new HttpGet(uri);
                    }
                }
            });
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor " + options.getThreadPrefix() + "not set up correctly", e);
        }

        HttpParams params = client.getParams();

        HttpProtocolParams.setUserAgent(params, getUserAgent(options));

        HttpConnectionParams.setConnectionTimeout(params, (int) options.getConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, (int) options.getSocketTimeout());
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setTcpNoDelay(params, true);

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(options.getMaxCacheEntries());
        cacheConfig.setSharedCache(false);
        cacheConfig.setMaxObjectSize(options.getMaxCacheObjectSize());
        cacheConfig.setNeverCache1_0ResponsesWithQueryString(false);

        this.nonCachingHttpClient = client;
        this.httpCacheStorage = new LoggingHttpCacheStorage(new FlushableHttpCacheStorageImpl(cacheConfig));
        httpClient = new CachingHttpAsyncClient(client, httpCacheStorage, cacheConfig);

        callbackExecutor = httpClientOptions.getCallbackExecutor();
        httpClient.start();
    }

    private AsyncSchemeRegistry getAsyncSchemeRegistryFactory(boolean trustSelfSignedCertificates)
    {
        AsyncSchemeRegistry registry = new AsyncSchemeRegistry();
        registry.register(new AsyncScheme("http", 80, null));
        registry.register(new AsyncScheme("https", 443, getSslLayeringStrategy(trustSelfSignedCertificates)));
        return registry;
    }

    private SSLLayeringStrategy getSslLayeringStrategy(boolean trustSelfSignedCertificates)
    {
        try
        {
            return new SSLLayeringStrategy(trustSelfSignedCertificates ? new TrustSelfSignedStrategy() : null);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getUserAgent(HttpClientOptions options)
    {
        return format("Atlassian HttpClient %s / %s / %s",
                httpClientVersion.get(),
                applicationName.get(),
                options.getUserAgent());
    }

    @Override
    public final ResponsePromise execute(final Request request)
    {
        try
        {
            return doExecute(request);
        }
        catch (Throwable t)
        {
            return ResponsePromises.toResponsePromise(rejected(t, Response.class));
        }
    }

    private ResponsePromise doExecute(final Request request)
    {
        httpClientOptions.getRequestPreparer().apply(request);

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        final Request.Method method = request.getMethod();
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
            new RequestEntityEffect(request).apply(op);
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet())
        {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        final PromiseHttpAsyncClient asyncClient = getPromiseHttpAsyncClient(request);
        return ResponsePromises.toResponsePromise(asyncClient.execute(op, new BasicHttpContext()).fold(
                new Function<Throwable, Response>()
                {
                    @Override
                    public Response apply(Throwable ex)
                    {
                        final long requestDuration = System.currentTimeMillis() - start;
                        publishEvent(request, requestDuration, ex);
                        throw Throwables.propagate(ex);
                    }
                },
                new Function<HttpResponse, Response>()
                {
                    @Override
                    public Response apply(HttpResponse httpResponse)
                    {
                        final long requestDuration = System.currentTimeMillis() - start;
                        publishEvent(request, requestDuration, httpResponse.getStatusLine().getStatusCode());
                        try
                        {
                            return translate(httpResponse);
                        }
                        catch (IOException e)
                        {
                            throw Throwables.propagate(e);
                        }
                    }
                }
        ));
    }

    private void publishEvent(Request request, long requestDuration, int statusCode)
    {
        if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code)
        {
            eventConsumer.apply(new HttpRequestCompletedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        }
        else
        {
            eventConsumer.apply(new HttpRequestFailedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        }
    }

    private void publishEvent(Request request, long requestDuration, Throwable ex)
    {
        eventConsumer.apply(new HttpRequestFailedEvent(
                request.getUri().toString(),
                request.getMethod().name(),
                ex.toString(),
                requestDuration,
                request.getAttributes()));
    }

    private PromiseHttpAsyncClient getPromiseHttpAsyncClient(Request request)
    {
        return new SettableFuturePromiseHttpPromiseAsyncClient<C>(request.isCacheDisabled() ? nonCachingHttpClient : httpClient, threadLocalContextManager, callbackExecutor);
    }

    private Response translate(HttpResponse httpResponse) throws IOException
    {
        StatusLine status = httpResponse.getStatusLine();
        Response.Builder responseBuilder = DefaultResponse.builder()
                .setMaxEntitySize(httpClientOptions.getMaxEntitySize())
                .setStatusCode(status.getStatusCode())
                .setStatusText(status.getReasonPhrase());

        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders)
        {
            responseBuilder.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            responseBuilder.setEntityStream(entity.getContent());
        }
        return responseBuilder.build();
    }

    @Override
    public void destroy() throws Exception
    {
        callbackExecutor.shutdown();
        httpClient.shutdown();
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpCacheStorage.flushByUriPattern(urlPattern);
    }

    private static final class NoOpThreadLocalContextManager<C> implements ThreadLocalContextManager<C>
    {
        @Override
        public C getThreadLocalContext()
        {
            return null;
        }

        @Override
        public void setThreadLocalContext(C context)
        {
        }

        @Override
        public void clearThreadLocalContext()
        {
        }
    }

    private static final class DefaultApplicationNameSupplier implements Supplier<String>
    {
        private final ApplicationProperties applicationProperties;

        public DefaultApplicationNameSupplier(ApplicationProperties applicationProperties)
        {
            this.applicationProperties = checkNotNull(applicationProperties);
        }

        @Override
        public String get()
        {
            return format("%s-%s (%s)",
                    applicationProperties.getDisplayName(),
                    applicationProperties.getVersion(),
                    applicationProperties.getBuildNumber());
        }
    }

    private static class EventConsumerFunction implements Function<Object, Void>
    {
        private final EventPublisher eventPublisher;

        public EventConsumerFunction(EventPublisher eventPublisher)
        {
            this.eventPublisher = eventPublisher;
        }

        @Override
        public Void apply(Object event)
        {
            eventPublisher.publish(event);
            return null;
        }
    }
}
