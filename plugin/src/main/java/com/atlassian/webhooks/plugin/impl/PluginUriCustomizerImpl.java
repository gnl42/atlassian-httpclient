package com.atlassian.webhooks.plugin.impl;

import com.atlassian.webhooks.spi.plugin.PluginUriCustomizer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class PluginUriCustomizerImpl implements PluginUriCustomizer
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext bundleContext;

    public PluginUriCustomizerImpl(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public URI customizeURI(String pluginKey, URI path, WebHookEvent webHookEvent)
    {
        final URI newUri = getFromOsgiService(pluginKey, path, webHookEvent);
        if (newUri != null)
        {
            logger.debug("Found new URI from OSGi service, '{}'", newUri);
            return newUri;
        }
        return path;
    }

    private URI getFromOsgiService(String pluginKey, URI path, WebHookEvent webHookEvent)
    {
        final ServiceReference newPluginUriCustomizerReference = bundleContext.getServiceReference(PluginUriCustomizer.class.getName());
        if (newPluginUriCustomizerReference != null)
        {
            try
            {
                final PluginUriCustomizer newUriResolver = (PluginUriCustomizer) bundleContext.getService(newPluginUriCustomizerReference);
                return newUriResolver.customizeURI(pluginKey, path, webHookEvent);
            }
            finally
            {
                bundleContext.ungetService(newPluginUriCustomizerReference);
            }
        }
        return null;
    }
}
