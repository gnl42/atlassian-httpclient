package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorage;
import com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorageImpl;
import com.atlassian.httpclient.apache.httpcomponents.cache.LoggingHttpCacheStorage;
import com.atlassian.httpclient.apache.httpcomponents.proxy.ProxyConfigFactory;
import com.atlassian.httpclient.apache.httpcomponents.proxy.ProxyCredentialsProvider;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.api.ResponseTooLargeException;
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
import com.google.common.primitives.Ints;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.atlassian.util.concurrent.Promises.rejected;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class ApacheAsyncHttpClient<C> extends AbstractHttpClient implements HttpClient, DisposableBean {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Supplier<String> httpClientVersion = Suppliers.memoize(
            () -> MavenUtils.getVersion("com.atlassian.httpclient", "atlassian-httpclient-api"));

    private final Function<Object, Void> eventConsumer;
    private final Supplier<String> applicationName;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final ExecutorService callbackExecutor;
    private final HttpClientOptions httpClientOptions;

    private final CachingHttpAsyncClient httpClient;
    private final CloseableHttpAsyncClient nonCachingHttpClient;
    private final FlushableHttpCacheStorage httpCacheStorage;

    public ApacheAsyncHttpClient(EventPublisher eventConsumer, ApplicationProperties applicationProperties,
                                 ThreadLocalContextManager<C> threadLocalContextManager) {
        this(eventConsumer, applicationProperties, threadLocalContextManager, new HttpClientOptions());
    }

    public ApacheAsyncHttpClient(EventPublisher eventConsumer,
                                 ApplicationProperties applicationProperties,
                                 ThreadLocalContextManager<C> threadLocalContextManager,
                                 HttpClientOptions options) {
        this(new DefaultApplicationNameSupplier(applicationProperties),
                new EventConsumerFunction(eventConsumer),
                threadLocalContextManager,
                options);
    }

    public ApacheAsyncHttpClient(String applicationName) {
        this(applicationName, new HttpClientOptions());
    }

    public ApacheAsyncHttpClient(String applicationName, final HttpClientOptions options) {
        this(Suppliers.ofInstance(applicationName), Functions.constant(null), new NoOpThreadLocalContextManager<>(), options);
    }

    public ApacheAsyncHttpClient(final Supplier<String> applicationName,
                                 final Function<Object, Void> eventConsumer,
                                 final ThreadLocalContextManager<C> threadLocalContextManager,
                                 final HttpClientOptions options) {
        this.eventConsumer = checkNotNull(eventConsumer);
        this.applicationName = checkNotNull(applicationName);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        this.httpClientOptions = checkNotNull(options);

        try {
            final IOReactorConfig reactorConfig = IOReactorConfig.custom()
                    .setIoThreadCount(options.getIoThreadCount())
                    .setSelectInterval(options.getIoSelectInterval())
                    .setInterestOpQueued(true)
                    .build();

            final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(reactorConfig);
            ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
                @Override
                public boolean handle(final IOException e) {
                    log.error("IO exception in reactor ", e);
                    return false;
                }

                @Override
                public boolean handle(final RuntimeException e) {
                    log.error("Fatal runtime error", e);
                    return false;
                }
            });

            final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
                    ioReactor,
                    ManagedNHttpClientConnectionFactory.INSTANCE,
                    getRegistry(options),
                    DefaultSchemePortResolver.INSTANCE,
                    host -> options.getHostHostResolver()
                            .orElse(DefaultHostResolver.INSTANCE)
                            .resolve(host),
                    options.getConnectionPoolTimeToLive(),
                    TimeUnit.MILLISECONDS) {
                @SuppressWarnings("MethodDoesntCallSuperMethod")
                @Override
                protected void finalize() {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            };

            final RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout((int) options.getConnectionTimeout())
                    .setConnectionRequestTimeout((int) options.getLeaseTimeout())
                    .setCookieSpec(options.getIgnoreCookies() ? CookieSpecs.IGNORE_COOKIES : CookieSpecs.DEFAULT)
                    .setSocketTimeout((int) options.getSocketTimeout())
                    .build();

            connectionManager.setDefaultMaxPerRoute(options.getMaxConnectionsPerHost());
            connectionManager.setMaxTotal(options.getMaxTotalConnections());

            final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                    .setThreadFactory(ThreadFactories.namedThreadFactory(options.getThreadPrefix() + "-io", ThreadFactories.Type.DAEMON))
                    .setDefaultIOReactorConfig(reactorConfig)
                    .setConnectionManager(connectionManager)
                    .setRedirectStrategy(new RedirectStrategy())
                    .setUserAgent(getUserAgent(options))
                    .setDefaultRequestConfig(requestConfig);

            // set up a route planner if there is proxy configuration
            ProxyConfigFactory.getProxyConfig(options).forEach(proxyConfig -> {
                // don't be fooled by its name. If SystemDefaultRoutePlanner is passed a proxy selector it will use that
                // instead of creating the default one that reads system properties
                clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE, proxyConfig.toProxySelector()));

                ProxyCredentialsProvider.build(options).forEach(credsProvider -> {
                    clientBuilder.setProxyAuthenticationStrategy(ProxyAuthenticationStrategy.INSTANCE);
                    clientBuilder.setDefaultCredentialsProvider(credsProvider);
                });
            });

            this.nonCachingHttpClient = new BoundedHttpAsyncClient(clientBuilder.build(),
                    Ints.saturatedCast(options.getMaxEntitySize()));

            final CacheConfig cacheConfig = CacheConfig.custom()
                    .setMaxCacheEntries(options.getMaxCacheEntries())
                    .setSharedCache(false)
                    .setNeverCacheHTTP10ResponsesWithQueryString(false)
                    .setMaxObjectSize(options.getMaxCacheObjectSize())
                    .build();

            this.httpCacheStorage = new LoggingHttpCacheStorage(new FlushableHttpCacheStorageImpl(cacheConfig));
            this.httpClient = new CachingHttpAsyncClient(nonCachingHttpClient, httpCacheStorage, cacheConfig);
            this.callbackExecutor = options.getCallbackExecutor();

            nonCachingHttpClient.start();
        } catch (IOReactorException e) {
            throw new RuntimeException("Reactor " + options.getThreadPrefix() + "not set up correctly", e);
        }
    }

    private Registry<SchemeIOSessionStrategy> getRegistry(final HttpClientOptions options) {
        try {
            final TrustSelfSignedStrategy strategy = options.trustSelfSignedCertificates() ?
                    new TrustSelfSignedStrategy() : null;

            final SSLContext sslContext = new SSLContextBuilder()
                    .useTLS()
                    .loadTrustMaterial(null, strategy)
                    .build();

            final SSLIOSessionStrategy sslioSessionStrategy = new SSLIOSessionStrategy(
                    sslContext,
                    split(System.getProperty("https.protocols")),
                    split(System.getProperty("https.cipherSuites")),
                    options.trustSelfSignedCertificates() ?
                            getSelfSignedVerifier() : SSLIOSessionStrategy.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

            return RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register("http", NoopIOSessionStrategy.INSTANCE)
                    .register("https", sslioSessionStrategy)
                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            return getFallbackRegistry(e);
        }
    }

    private X509HostnameVerifier getSelfSignedVerifier() {
        return new X509HostnameVerifier() {
            @Override
            public void verify(final String host, final SSLSocket ssl) {
                log.debug("Verification for certificates from {0} disabled", host);
            }

            @Override
            public void verify(final String host, final X509Certificate cert) {
                log.debug("Verification for certificates from {0} disabled", host);
            }

            @Override
            public void verify(final String host, final String[] cns, final String[] subjectAlts) {
                log.debug("Verification for certificates from {0} disabled", host);
            }

            @Override
            public boolean verify(final String host, final SSLSession sslSession) {
                log.debug("Verification for certificates from {0} disabled", host);
                return true;
            }
        };
    }

    private Registry<SchemeIOSessionStrategy> getFallbackRegistry(final GeneralSecurityException e) {
        log.error("Error when creating scheme session strategy registry", e);
        return RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", SSLIOSessionStrategy.getDefaultStrategy())
                .build();
    }

    private String getUserAgent(HttpClientOptions options) {
        return format("Atlassian HttpClient %s / %s / %s",
                httpClientVersion.get(),
                applicationName.get(),
                options.getUserAgent());
    }

    @Override
    public final ResponsePromise execute(final Request request) {
        try {
            return doExecute(request);
        } catch (Throwable t) {
            return ResponsePromises.toResponsePromise(rejected(t, Response.class));
        }
    }

    private ResponsePromise doExecute(final Request request) {
        httpClientOptions.getRequestPreparer().apply(request);

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        final Request.Method method = request.getMethod();
        switch (method) {
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
        if (request.hasEntity()) {
            new RequestEntityEffect(request).apply(op);
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        final PromiseHttpAsyncClient asyncClient = getPromiseHttpAsyncClient(request);
        return ResponsePromises.toResponsePromise(asyncClient.execute(op, new BasicHttpContext()).fold(
                ex -> {
                    final long requestDuration = System.currentTimeMillis() - start;
                    Throwable exception = maybeTranslate(ex);
                    publishEvent(request, requestDuration, exception);
                    throw Throwables.propagate(exception);
                },
                httpResponse -> {
                    final long requestDuration = System.currentTimeMillis() - start;
                    publishEvent(request, requestDuration, httpResponse.getStatusLine().getStatusCode());
                    try {
                        return translate(httpResponse);
                    } catch (IOException e) {
                        throw Throwables.propagate(e);
                    }
                }
        ));
    }

    private void publishEvent(Request request, long requestDuration, int statusCode) {
        if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code) {
            eventConsumer.apply(new HttpRequestCompletedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        } else {
            eventConsumer.apply(new HttpRequestFailedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        }
    }

    private void publishEvent(Request request, long requestDuration, Throwable ex) {
        eventConsumer.apply(new HttpRequestFailedEvent(
                request.getUri().toString(),
                request.getMethod().name(),
                ex.toString(),
                requestDuration,
                request.getAttributes()));
    }

    private PromiseHttpAsyncClient getPromiseHttpAsyncClient(Request request) {
        return new SettableFuturePromiseHttpPromiseAsyncClient<>(
                request.isCacheDisabled() ? nonCachingHttpClient : httpClient,
                threadLocalContextManager, callbackExecutor);
    }

    private Throwable maybeTranslate(Throwable ex) {
        if (ex instanceof EntityTooLargeException) {
            EntityTooLargeException tooLarge = (EntityTooLargeException) ex;
            try {
                // don't include the cause to ensure that the HttpResponse is released
                return new ResponseTooLargeException(translate(tooLarge.getResponse()), ex.getMessage());
            } catch (IOException e) {
                // could not translate, just return the original exception
            }
        }
        return ex;
    }

    private Response translate(HttpResponse httpResponse) throws IOException {
        StatusLine status = httpResponse.getStatusLine();
        Response.Builder responseBuilder = DefaultResponse.builder()
                .setMaxEntitySize(httpClientOptions.getMaxEntitySize())
                .setStatusCode(status.getStatusCode())
                .setStatusText(status.getReasonPhrase());

        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders) {
            responseBuilder.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            responseBuilder.setEntityStream(entity.getContent());
        }
        return responseBuilder.build();
    }

    @Override
    public void destroy() throws Exception {
        callbackExecutor.shutdown();
        nonCachingHttpClient.close();
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern) {
        httpCacheStorage.flushByUriPattern(urlPattern);
    }

    private static final class NoOpThreadLocalContextManager<C> implements ThreadLocalContextManager<C> {
        @Override
        public C getThreadLocalContext() {
            return null;
        }

        @Override
        public void setThreadLocalContext(C context) {
        }

        @Override
        public void clearThreadLocalContext() {
        }
    }

    private static final class DefaultApplicationNameSupplier implements Supplier<String> {
        private final ApplicationProperties applicationProperties;

        public DefaultApplicationNameSupplier(ApplicationProperties applicationProperties) {
            this.applicationProperties = checkNotNull(applicationProperties);
        }

        @Override
        public String get() {
            return format("%s-%s (%s)",
                    applicationProperties.getDisplayName(),
                    applicationProperties.getVersion(),
                    applicationProperties.getBuildNumber());
        }
    }

    private static class EventConsumerFunction implements Function<Object, Void> {
        private final EventPublisher eventPublisher;

        public EventConsumerFunction(EventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Override
        public Void apply(Object event) {
            eventPublisher.publish(event);
            return null;
        }
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }
}
