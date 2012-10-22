package com.atlassian.webhooks.plugin.junit;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.plugin.test.CheckThreadContext;
import com.atlassian.webhooks.plugin.test.ServiceAccessor;
import com.google.common.base.Function;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class TestHttpClientThreadContext
{
    private HttpClient httpClient = ServiceAccessor.httpClient;
    private ApplicationProperties applicationProperties = ServiceAccessor.applicationProperties;
    private CheckThreadContext checkThreadContext = ServiceAccessor.checkThreadContext;

    @Test
    public void testTransformFunctionHasThreadContext()
    {
        checkThreadContext.before();

        final AtomicBoolean called = new AtomicBoolean(false);
        httpClient.newRequest(applicationProperties.getBaseUrl()).get().transform().done(new Function<Response, Object>()
        {
            @Override
            public Object apply(Response response)
            {
                checkThreadContext.check();
                called.set(true);
                return null;
            }
        }).claim();

        assertTrue(called.get());
    }
}
