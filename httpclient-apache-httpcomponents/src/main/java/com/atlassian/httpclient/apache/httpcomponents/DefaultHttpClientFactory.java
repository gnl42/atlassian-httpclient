package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.DisposableBean;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.*;

public final class DefaultHttpClientFactory implements HttpClientFactory, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final ThreadLocalContextManager threadLocalContextManager;
    private final Set<DefaultHttpClient> httpClients = new CopyOnWriteArraySet<DefaultHttpClient>();

    public DefaultHttpClientFactory(EventPublisher eventPublisher, ApplicationProperties applicationProperties, ThreadLocalContextManager threadLocalContextManager)
    {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
    }

    @Override
    public HttpClient create(HttpClientOptions options)
    {
        return doCreate(options, threadLocalContextManager);
    }

    @Override
    public HttpClient create(HttpClientOptions options, ThreadLocalContextManager threadLocalContextManager)
    {
        return doCreate(options, threadLocalContextManager);
    }

    private HttpClient doCreate(HttpClientOptions options, ThreadLocalContextManager threadLocalContextManager)
    {
        checkNotNull(options);
        final DefaultHttpClient httpClient = new DefaultHttpClient(eventPublisher, applicationProperties, threadLocalContextManager, options);
        httpClients.add(httpClient);
        return httpClient;
    }

    @Override
    public void destroy() throws Exception
    {
        for (DefaultHttpClient httpClient : httpClients)
        {
            httpClient.destroy();
        }
    }
}
