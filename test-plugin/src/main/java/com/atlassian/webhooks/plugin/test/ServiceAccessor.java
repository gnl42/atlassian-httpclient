package com.atlassian.webhooks.plugin.test;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.api.provider.WebHookListenerService;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServiceAccessor
{
    public static EventPublisher eventPublisher;
    public static ApplicationProperties applicationProperties;
    public static HttpClient httpClient;
    public static CheckThreadContext checkThreadContext;
    public static WebHookListenerService webHookListenerService;

    public ServiceAccessor(EventPublisher eventPublisher,
                           ApplicationProperties applicationProperties,
                           HttpClient httpClient,
                           CheckThreadContext checkThreadContext,
                            WebHookListenerService webHookListenerService)
    {
        ServiceAccessor.eventPublisher = checkNotNull(eventPublisher);
        ServiceAccessor.applicationProperties = checkNotNull(applicationProperties);
        ServiceAccessor.httpClient = checkNotNull(httpClient);
        ServiceAccessor.checkThreadContext = checkNotNull(checkThreadContext);
        ServiceAccessor.webHookListenerService = checkNotNull(webHookListenerService);
    }
}
