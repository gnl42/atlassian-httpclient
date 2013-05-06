package com.atlassian.webhooks.spi.plugin;

import com.google.common.base.Optional;

import java.net.URI;

public interface PluginUriResolver
{
    /**
     * Gets a fully constructed URI for a relative path defined in the plugin with the given key.
     *
     * @param pluginKey the key of the plugin we're resolving the URI for.
     * @param path the relative path
     * @return an absolute URI to the plugin path.
     */
    Optional<URI> getUri(String pluginKey, URI path);
}
