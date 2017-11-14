package com.atlassian.httpclient.apache.httpcomponents;

import com.google.common.primitives.Ints;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.Future;

public class BoundedHttpAsyncClient extends CloseableHttpAsyncClient {

    private final CloseableHttpAsyncClient delegate;
    private final int maxEntitySize;

    public BoundedHttpAsyncClient(CloseableHttpAsyncClient delegate, int maxEntitySize) {
        this.delegate = delegate;
        this.maxEntitySize = maxEntitySize;
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer,
                                 HttpAsyncResponseConsumer<T> responseConsumer,
                                 HttpContext context,
                                 FutureCallback<T> callback) {
        return delegate.execute(requestProducer, responseConsumer, context, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, HttpContext context,
                                        FutureCallback<HttpResponse> callback) {
        BoundedAsyncResponseConsumer consumer = new BoundedAsyncResponseConsumer(Ints.saturatedCast(maxEntitySize));
        return delegate.execute(
                HttpAsyncMethods.create(target, request),
                consumer,
                context, callback);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
