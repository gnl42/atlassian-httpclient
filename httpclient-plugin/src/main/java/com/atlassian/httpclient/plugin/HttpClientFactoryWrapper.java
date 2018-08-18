package com.atlassian.httpclient.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class HttpClientFactoryWrapper implements ServiceFactory {
    private com.atlassian.httpclient.api.factory.HttpClientFactory defaultHttpClientFactory;
            
    public HttpClientFactoryWrapper(com.atlassian.httpclient.api.factory.HttpClientFactory httpClientFactory) {
        this.defaultHttpClientFactory = httpClientFactory;
    }

    @Override
    public HttpClient getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return defaultHttpClientFactory.create(new HttpClientOptions());
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {

    }
}
