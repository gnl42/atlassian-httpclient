package com.atlassian.httpclient.api.factory;

/**
 * Creates {@link SettableFutureHandler} instances for every request
 */
public interface SettableFutureHandlerFactory<V>
{
    /**
     * @return A new instance
     */
    SettableFutureHandler<V> create();
}
