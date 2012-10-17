package com.atlassian.httpclient.test;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.google.common.util.concurrent.SettableFuture;

import java.util.regex.Pattern;

public final class SleepingHttpClient extends AbstractHttpClient
{
    @Override
    public ResponsePromise execute(DefaultRequest request)
    {
        try
        {
            Thread.sleep(100000);
            return ResponsePromises.toResponsePromise(SettableFuture.<Response>create());
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
    }
}
