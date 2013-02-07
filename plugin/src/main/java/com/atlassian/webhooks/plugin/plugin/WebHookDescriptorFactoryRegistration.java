package com.atlassian.webhooks.plugin.plugin;

import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookDescriptorFactoryRegistration implements InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PluginAccessor pluginAccessor;
    private final ListableModuleDescriptorFactory moduleDescriptorFactory;
    private final BundleContext bundleContext;

    private ServiceRegistration serviceRegistration;

    public WebHookDescriptorFactoryRegistration(ListableModuleDescriptorFactory moduleDescriptorFactory, PluginAccessor pluginAccessor, BundleContext bundleContext)
    {
        this.moduleDescriptorFactory = checkNotNull(moduleDescriptorFactory);
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (!isSchemaPluginInstalled())
        {
            logger.debug("Schema plugin is not installed. Registering {}.", moduleDescriptorFactory.getModuleDescriptorClasses());

            serviceRegistration = bundleContext.registerService(
                    new String[]{ListableModuleDescriptorFactory.class.getName(), ModuleDescriptorFactory.class.getName()},
                    moduleDescriptorFactory,
                    null);
        }
        else
        {
            logger.debug("Schema plugin is installed. Not registering {}.", moduleDescriptorFactory.getModuleDescriptorClasses());
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

    private boolean isSchemaPluginInstalled()
    {
        // we check whether the plugin is installed, not whether it's enabled since it could get enabled after the
        // webhook plugin
        return pluginAccessor.getPlugin("com.atlassian.plugins.schema") != null;
    }
}
