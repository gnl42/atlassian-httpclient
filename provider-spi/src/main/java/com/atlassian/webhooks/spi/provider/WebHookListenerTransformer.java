package com.atlassian.webhooks.spi.provider;

import com.google.common.base.Optional;

/**
 * Contract for transformation of an internal WebHook representation into representation on which WebHookProvider can
 * operate.
 */
public interface WebHookListenerTransformer
{

    /**
     * Transforms the listener parameters into {@link com.atlassian.webhooks.spi.provider.WebHookListener}. Transformed
     * listener should be recognizable by the event matcher registered by WebHook listener provider.
     *
     * @param webHookListenerParameters Parameters to transform into WebookListener.
     * @return Either transformed WebHook or None, if implementation of this interface can't transform given WebHook
     * parameters.
     */
    Optional<WebHookListener> transform(WebHookListenerParameters webHookListenerParameters);

}
