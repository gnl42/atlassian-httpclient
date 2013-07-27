package com.atlassian.webhooks.plugin.store;

import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.store.WebHookListenerStore;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegates all store methods to the service found in the bundleContext.
 */
public class OsgiWebHookListenerStore implements WebHookListenerStore
{
    private final BundleContext bundleContext;

    public OsgiWebHookListenerStore(final BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public WebHookListenerParameters addWebHook(
            @NotNull final String name,
            @NotNull final String targetUrl,
            @NotNull final Iterable<String> events,
            final String params,
            final String registrationMethod)
    {
        return executeStoreFunction(new Function<WebHookListenerStore, WebHookListenerParameters>()
        {
            @Override
            public WebHookListenerParameters apply(final WebHookListenerStore store)
            {
                return store.addWebHook(name, targetUrl, events, params, registrationMethod);
            }
        });
    }

    @Override
    public WebHookListenerParameters updateWebHook(
            final int id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final String params,
            final boolean enabled) throws IllegalArgumentException
    {
        return executeStoreFunction(new Function<WebHookListenerStore, WebHookListenerParameters>()
        {
            @Override
            public WebHookListenerParameters apply(final WebHookListenerStore store)
            {
                return store.updateWebHook(id, name, targetUrl, events, params, enabled);
            }
        });
    }

    @Override
    public Optional<WebHookListenerParameters> getWebHook(final int id)
    {
        return executeStoreFunction(new Function<WebHookListenerStore, Optional<WebHookListenerParameters>>()
        {
            @Override
            public Optional<WebHookListenerParameters> apply(WebHookListenerStore store)
            {
                return store.getWebHook(id);
            }
        });
    }

    @Override
    public void removeWebHook(final int id) throws IllegalArgumentException
    {
        executeStoreFunction(new Function<WebHookListenerStore, Void>()
        {
            @Override
            public Void apply(final WebHookListenerStore store)
            {
                store.removeWebHook(id);
                return null;
            }
        });
    }

    @Override
    public Collection<WebHookListenerParameters> getAllWebHooks()
    {
        return executeStoreFunction(new Function<WebHookListenerStore, Collection<WebHookListenerParameters>>()
        {
            @Override
            public Collection<WebHookListenerParameters> apply(final WebHookListenerStore store)
            {
                return store.getAllWebHooks();
            }
        });
    }

    public Optional<WebHookListenerParameters> enableWebHook(final int id, final boolean enabled)
    {
        return executeStoreFunction(new Function<WebHookListenerStore, Optional<WebHookListenerParameters>>()
        {
            @Override
            public Optional<WebHookListenerParameters> apply(final WebHookListenerStore store)
            {
                return store.enableWebHook(id, enabled);
            }
        });
    }

    private <T> T executeStoreFunction(Function<WebHookListenerStore, T> storeFunction)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookListenerStore.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookListenerStore webHookListenerStore = (WebHookListenerStore) bundleContext.getService(serviceReference);
                return storeFunction.apply(webHookListenerStore);
            }
            finally
            {
                bundleContext.ungetService(serviceReference);
            }
        }
        else
        {
            throw new IllegalStateException("Couldn't find an implementation of WebHookListenerStore");
        }
    }
}
