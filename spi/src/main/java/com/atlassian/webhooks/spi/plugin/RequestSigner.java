package com.atlassian.webhooks.spi.plugin;

import com.atlassian.httpclient.api.Request;

/**
 * Signs outgoing requests when publishing webhooks
 */
public interface RequestSigner
{
    /**
     * Signs the requests
     * @param pluginKey The remote plugin key of the target system, may be null
     * @param request The request to sign
     */
    void sign(String pluginKey, Request request);
}
