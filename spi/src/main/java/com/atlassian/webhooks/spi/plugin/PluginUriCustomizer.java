package com.atlassian.webhooks.spi.plugin;

import com.atlassian.webhooks.spi.provider.WebHookEvent;

import java.net.URI;

public interface PluginUriCustomizer
{

    /**
     * @param pluginKey the key of the plugin we're customizing the URI for.
     * @param path the absolute URI to the plugin path built by PluginUriResolver.
     * @param webHookEvent event for which we're customizing the URI.
     * @return an absolute URI to the plugin path.
     */
    URI customizeURI(String pluginKey, URI path, WebHookEvent webHookEvent);

}
