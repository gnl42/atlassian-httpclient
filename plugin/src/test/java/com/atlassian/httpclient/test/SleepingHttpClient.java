package com.atlassian.httpclient.test;

import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.google.common.util.concurrent.SettableFuture;

import java.util.regex.Pattern;

import static com.atlassian.httpclient.api.ResponsePromises.*;
import static com.atlassian.util.concurrent.Promises.*;

public final class SleepingHttpClient extends AbstractHttpClient
{
    @Override
    public ResponsePromise execute(DefaultRequest request)
    {
        try
        {
            Thread.sleep(100000);
            final SettableFuture<Response> future = SettableFuture.create();
            return toResponsePromise(forListenableFuture(future));
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
