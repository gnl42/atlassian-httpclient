package com.atlassian.webhooks.plugin.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.webhooks.spi.provider.ConsumerKey;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import org.dom4j.Element;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry;

    private String eventIdentifier;
    private URI url;
    private String moduleKey;

    public WebHookModuleDescriptor(ModuleFactory moduleFactory, ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry)
    {
        super(moduleFactory);
        this.webHookConsumerRegistry = checkNotNull(webHookConsumerRegistry);
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        eventIdentifier = getOptionalAttribute(element, "event", getKey());
        url = getRequiredUriAttribute(element, "url");
        moduleKey = getRequiredAttribute(element, "key");
    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookConsumerRegistry.register(eventIdentifier, new ConsumerKey(getPluginKey(), moduleKey), url);
    }

    @Override
    public void disabled()
    {
        webHookConsumerRegistry.unregister(eventIdentifier, new ConsumerKey(getPluginKey(), moduleKey), url);
        super.disabled();
    }

    private static String getOptionalAttribute(Element e, String name, Object defaultValue)
    {
        String value = e.attributeValue(name);
        return value != null ? value :
                defaultValue != null ? defaultValue.toString() : null;
    }

    private static URI getRequiredUriAttribute(Element e, String name)
    {
        String value = getRequiredAttribute(e, name);
        return URI.create(value);
    }

    private static String getRequiredAttribute(Element e, String name)
    {
        String value = e.attributeValue(name);
        if (value == null)
        {
            throw new PluginParseException("Attribute '" + name + "' is required on '" + e.getName() + "'");
        }
        return value;
    }
}
