package com.atlassian.webhooks.plugin.plugin;

import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookDescriptorFactoryRegistration implements InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ListableModuleDescriptorFactory moduleDescriptorFactory;
    private final BundleContext bundleContext;
    private final Supplier<Boolean> describedModuleTypeDeclared;

    private ServiceRegistration serviceRegistration;

    public WebHookDescriptorFactoryRegistration(ListableModuleDescriptorFactory moduleDescriptorFactory, BundleContext bundleContext)
    {
        this.moduleDescriptorFactory = checkNotNull(moduleDescriptorFactory);
        this.bundleContext = checkNotNull(bundleContext);
        this.describedModuleTypeDeclared = Suppliers.memoize(new DescribedModuleTypeDeclaredSupplier(bundleContext));
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (!isDescribedModuleTypeDeclared())
        {
            logger.debug("Described Module Type is not installed. Registering {}.", moduleDescriptorFactory.getModuleDescriptorClasses());

            serviceRegistration = bundleContext.registerService(
                    new String[]{ListableModuleDescriptorFactory.class.getName(), ModuleDescriptorFactory.class.getName()},
                    moduleDescriptorFactory,
                    null);
        }
        else
        {
            logger.debug("Described Module Type is installed. Not registering {}.", moduleDescriptorFactory.getModuleDescriptorClasses());
        }
    }

    @Override
    public void destroy() throws Exception
    {
        if (serviceRegistration != null)
        {
            serviceRegistration.unregister();
        }
    }

    private boolean isDescribedModuleTypeDeclared()
    {

        return describedModuleTypeDeclared.get();
    }

    private static final class DescribedModuleTypeDeclaredSupplier implements Supplier<Boolean>
    {
        private final BundleContext bundleContext;

        private DescribedModuleTypeDeclaredSupplier(BundleContext bundleContext)
        {
            this.bundleContext = checkNotNull(bundleContext);
        }

        @Override
        public Boolean get()
        {
            final ServiceReference[] serviceReferences = getServiceReferences(bundleContext, DescribedModuleDescriptorFactory.class);
            if (serviceReferences == null)
            {
                return false;
            }

            for (ServiceReference reference : serviceReferences)
            {
                final ListableModuleDescriptorFactory factory = getService(bundleContext, reference, ListableModuleDescriptorFactory.class);
                System.out.println(factory + ":" + factory.getModuleDescriptorClasses());
                if (factory.hasModuleDescriptor("described-module-type"))
                {
                    ungetServiceReferences(bundleContext, serviceReferences);
                    return true;
                }
            }

            ungetServiceReferences(bundleContext, serviceReferences);
            return false;
        }
    }

    private static <T> T getService(BundleContext bundleContext, ServiceReference reference, Class<T> type)
    {
        return type.cast(bundleContext.getService(reference));
    }

    private static ServiceReference[] getServiceReferences(BundleContext bundleContext, Class<?> type)
    {
        try
        {
            return bundleContext.getAllServiceReferences(type.getName(), null);
        }
        catch (InvalidSyntaxException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static ServiceReference[] ungetServiceReferences(BundleContext bundleContext, ServiceReference[] srs)
    {
        for (ServiceReference sr : srs)
        {
            bundleContext.ungetService(sr);
        }
        return srs;
    }
}
