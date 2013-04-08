package com.atlassian.webhooks.plugin.impl;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.*;

public final class PluginUriResolverImpl implements PluginUriResolver
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;

    public PluginUriResolverImpl(ApplicationProperties applicationProperties, BundleContext bundleContext)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public URI getUri(String pluginKey, URI path)
    {
        try
        {
            final URI newUri = getFromOsgiService(pluginKey, path);
            if (newUri != null)
            {
                logger.debug("Found new URI from OSGi service, '{}'", newUri);
                return newUri;
            }
        }
        catch (InvalidSyntaxException e)
        {
            logger.error(e.getLocalizedMessage());
        }

        final URI defaultNewUri = getUriDefault(path);
        logger.debug("Found new URI from default Application properties, '{}'", defaultNewUri);
        return defaultNewUri;
    }

    private URI getFromOsgiService(String pluginKey, URI path) throws InvalidSyntaxException
    {
        final ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(PluginUriResolver.class.getName(), "");
        if (serviceReferences != null)
        {
            for (ServiceReference serviceReference : serviceReferences)
            {
                try
                {
                    final PluginUriResolver newUriResolver = (PluginUriResolver) bundleContext.getService(serviceReference);
                    URI uri = newUriResolver.getUri(pluginKey, path);
                    if (uri != null)
                    {
                        return uri;
                    }
                }
                finally
                {
                    bundleContext.ungetService(serviceReference);
                }
            }
        }
        return null;
    }

    private URI getUriDefault(URI path)
    {
        final String newUri = applicationProperties.getBaseUrl() + path.toString();
        try
        {
            return new URI(newUri);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("Could not parse the new URI, " + newUri, e);
        }
    }
}
