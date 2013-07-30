package com.atlassian.httpclient.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.httpclient.spi.ThreadLocalContextManagers;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.memoize;

public final class OsgiThreadLocalContextManager<C> implements ThreadLocalContextManager<C>, DisposableBean
{
    private final BundleContext bundleContext;

    private final Supplier<Option<ServiceReference>> serviceReference = memoize(new Supplier<Option<ServiceReference>>()
    {
        @Override
        public Option<ServiceReference> get()
        {
            return Option.option(bundleContext.getServiceReference(ThreadLocalContextManager.class.getName()));
        }
    });

    private final Supplier<ThreadLocalContextManager<C>> threadLocalContextManagerSupplier = memoize(new Supplier<ThreadLocalContextManager<C>>()
    {
        @Override
        public ThreadLocalContextManager<C> get()
        {
            return serviceReference.get().fold(
                    Suppliers.ofInstance(ThreadLocalContextManagers.<C>noop()),
                    new Function<ServiceReference, ThreadLocalContextManager<C>>()
                    {
                        @Override
                        public ThreadLocalContextManager<C> apply(ServiceReference input)
                        {
                            return (ThreadLocalContextManager<C>) bundleContext.getService(input);
                        }
                    });
        }
    });

    public OsgiThreadLocalContextManager(final BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public C getThreadLocalContext()
    {
        return threadLocalContextManagerSupplier.get().getThreadLocalContext();
    }

    @Override
    public void setThreadLocalContext(C context)
    {
        threadLocalContextManagerSupplier.get().setThreadLocalContext(context);
    }

    @Override
    public void resetThreadLocalContext()
    {
        threadLocalContextManagerSupplier.get().resetThreadLocalContext();
    }

    @Override
    public void destroy() throws Exception
    {
        serviceReference.get().map(new Function<ServiceReference, Boolean>()
        {
            @Override
            public Boolean apply(@Nullable ServiceReference input)
            {
                return bundleContext.ungetService(input);
            }
        });
    }
}
