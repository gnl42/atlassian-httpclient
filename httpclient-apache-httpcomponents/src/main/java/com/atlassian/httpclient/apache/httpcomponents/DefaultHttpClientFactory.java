package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import org.springframework.beans.factory.DisposableBean;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultHttpClientFactory implements HttpClientFactory, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final Set<DefaultHttpClient> httpClients = new CopyOnWriteArraySet<DefaultHttpClient>();

    public DefaultHttpClientFactory(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public HttpClient create(HttpClientOptions options)
    {
        checkNotNull(options);
        DefaultHttpClient httpClient = new DefaultHttpClient(eventPublisher, options);
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
