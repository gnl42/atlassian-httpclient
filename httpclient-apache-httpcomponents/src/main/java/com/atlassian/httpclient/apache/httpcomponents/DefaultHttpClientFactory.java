package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.beans.factory.DisposableBean;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.*;

public class DefaultHttpClientFactory implements HttpClientFactory, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final Set<DefaultHttpClient> httpClients = new CopyOnWriteArraySet<DefaultHttpClient>();

    public DefaultHttpClientFactory(EventPublisher eventPublisher, ApplicationProperties applicationProperties)
    {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.applicationProperties = checkNotNull(applicationProperties);
    }

    @Override
    public HttpClient create(HttpClientOptions options)
    {
        checkNotNull(options);
        DefaultHttpClient httpClient = new DefaultHttpClient(eventPublisher, applicationProperties, options);
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
