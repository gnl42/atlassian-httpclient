package com.atlassian.webhooks.plugin.store;

import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.store.WebHookListenerStore;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
            final Map<String, Object> params,
            final String registrationMethod)
    {
        return executeStoreFunction(new Function<WebHookListenerStore, WebHookListenerParameters>()
        {
            @Override
            public WebHookListenerParameters apply(final WebHookListenerStore store)
            {
                return store.addWebHook(name, targetUrl, events, params, registrationMethod);
            }
        }, null);
    }

    @Override
    public WebHookListenerParameters updateWebHook(
            final int id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final Map<String, Object> params,
            final boolean enabled) throws IllegalArgumentException
    {
        return executeStoreFunction(new Function<WebHookListenerStore, WebHookListenerParameters>()
        {
            @Override
            public WebHookListenerParameters apply(final WebHookListenerStore store)
            {
                return store.updateWebHook(id, name, targetUrl, events, params, enabled);
            }
        }, null);
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
        }, Optional.<WebHookListenerParameters>absent());
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
        }, null);
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
        }, Collections.<WebHookListenerParameters>emptyList());
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
        }, Optional.<WebHookListenerParameters>absent());
    }

    private <T> T executeStoreFunction(Function<WebHookListenerStore, T> storeFunction, final T defaultValue)
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
            return defaultValue;
        }
    }
}
