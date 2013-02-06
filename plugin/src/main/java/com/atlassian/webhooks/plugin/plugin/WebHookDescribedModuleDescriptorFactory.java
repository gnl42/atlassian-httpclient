package com.atlassian.webhooks.plugin.plugin;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.plugin.schema.plugin.DescribedModuleTypeModuleDescriptor;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookDescribedModuleDescriptorFactory implements ListableModuleDescriptorFactory
{
    @SuppressWarnings("unchecked")
    private static final Class<ModuleDescriptor<?>> DESCRIBED_MODULE_TYPE_MODULE_DESCRIPTOR_CLASS = (Class) DescribedModuleTypeModuleDescriptor.class;

    private static final String DESCRIBED_MODULE_TYPE = "described-module-type";

    private final PluginAccessor pluginAccessor;
    private final ModuleFactory moduleFactory;
    private final BundleContext bundleContext;

    private final Supplier<DescribedModuleTypeModuleDescriptor> moduleDescriptor = Suppliers.memoize(new Supplier<DescribedModuleTypeModuleDescriptor>()
    {
        @Override
        public DescribedModuleTypeModuleDescriptor get()
        {
            return new DescribedModuleTypeModuleDescriptor(moduleFactory, bundleContext);
        }
    });

    public WebHookDescribedModuleDescriptorFactory(PluginAccessor pluginAccessor, ModuleFactory moduleFactory, BundleContext bundleContext)
    {
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.moduleFactory = checkNotNull(moduleFactory);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(String s) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        return isValidType(s) ? moduleDescriptor.get() : null;
    }

    @Override
    public Class<? extends ModuleDescriptor> getModuleDescriptorClass(String s)
    {
        return isValidType(s) ? DESCRIBED_MODULE_TYPE_MODULE_DESCRIPTOR_CLASS : null;
    }

    @Override
    public boolean hasModuleDescriptor(String s)
    {
        return isValidType(s);
    }

    @Override
    public Set<Class<ModuleDescriptor<?>>> getModuleDescriptorClasses()
    {
        if (describedModuleDescriptorTypeDoesNotExist())
        {
            return ImmutableSet.of(DESCRIBED_MODULE_TYPE_MODULE_DESCRIPTOR_CLASS);
        }
        else
        {
            return ImmutableSet.of();
        }
    }

    private boolean isValidType(String s)
    {
        return DESCRIBED_MODULE_TYPE.equals(s) && describedModuleDescriptorTypeDoesNotExist();
    }

    private boolean describedModuleDescriptorTypeDoesNotExist()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByType(DESCRIBED_MODULE_TYPE).isEmpty();
    }
}
