package com.atlassian.httpclient.apache.httpcomponents.cache;

import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;

import java.io.IOException;

public abstract class RedirectingHttpCacheStorage implements HttpCacheStorage {

    protected abstract HttpCacheStorage delegate();

    @Override
    public void putEntry(final String key, final HttpCacheEntry entry) throws IOException {
        delegate().putEntry(key, entry);
    }

    @Override
    public HttpCacheEntry getEntry(final String key) throws IOException {
        return delegate().getEntry(key);
    }

    @Override
    public void removeEntry(final String key) throws IOException {
        delegate().removeEntry(key);
    }

    @Override
    public void updateEntry(final String key, final HttpCacheUpdateCallback callback) throws IOException, HttpCacheUpdateException {
        delegate().updateEntry(key, callback);
    }
}
