package com.atlassian.httpclient.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * This wrapper is used to make HttpClientFactory implement ServiceFactory to make it possible to export not only factory
 * itself, but also httpclient as a service
 */
public class HttpClientFactoryWrapper implements ServiceFactory {
    private HttpClientFactory defaultHttpClientFactory;
            
    public HttpClientFactoryWrapper(HttpClientFactory httpClientFactory) {
        this.defaultHttpClientFactory = httpClientFactory;
    }

    @Override
    public HttpClient getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setIgnoreCookies(true);
        return defaultHttpClientFactory.create(httpClientOptions);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {

    }
}