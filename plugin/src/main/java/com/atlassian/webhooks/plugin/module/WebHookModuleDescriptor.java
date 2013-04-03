package com.atlassian.webhooks.plugin.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleConsumerParams;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WebHookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ModuleDescriptorWebHookConsumerRegistry webHookConsumerRegistry;

    private String eventIdentifier;
    private URI url;
    private String moduleKey;
    private Map<String, Object> moduleParams;

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
        final ImmutableMap.Builder<String, Object> params = new ImmutableMap.Builder<String, Object>();

        List<Element> elements = element.elements("param");
        if (elements != null)
        {
            for (Element param : elements)
            {
                params.put(getRequiredAttribute(param, "key"), getRequiredAttribute(param, "value"));
            }
        }
        moduleParams = params.build();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookConsumerRegistry.register(eventIdentifier, getPluginKey(), url, new PluginModuleConsumerParams(getPluginKey(), Optional.of(moduleKey), moduleParams));
    }

    @Override
    public void disabled()
    {
        webHookConsumerRegistry.unregister(eventIdentifier, getPluginKey(), url, new PluginModuleConsumerParams(getPluginKey(), Optional.of(moduleKey), moduleParams));
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
