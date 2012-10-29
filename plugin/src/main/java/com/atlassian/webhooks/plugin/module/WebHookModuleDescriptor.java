package com.atlassian.webhooks.plugin.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.webhooks.plugin.ModuleDescriptorWebHookConsumerRegistry;
import org.dom4j.Element;

import java.net.URI;

import static com.google.common.base.Preconditions.*;

public final class WebHookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry;

    private String eventIdentifier;
    private URI url;

    public WebHookModuleDescriptor(ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry)
    {
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
    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookConsumerRegistry.register(getPluginKey(), eventIdentifier, url);
    }

    @Override
    public void disabled()
    {
        webHookConsumerRegistry.unregister(getPluginKey(), eventIdentifier, url);
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
