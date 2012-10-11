package com.atlassian.webhooks.plugin.module;

import com.atlassian.webhooks.plugin.WebHookPublisher;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializers;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;

import java.net.URI;

public final class WebHookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private String eventIdentifier;
    private URI url;
    private final WebHookPublisher webHookPublisher;
    private final StartableForPlugins startableForPlugins;
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    public WebHookModuleDescriptor(WebHookPublisher webHookPublisher,
                                   StartableForPlugins startableForPlugins,
                                   ApplicationProperties applicationProperties,
                                   ConsumerService consumerService)
    {
        this.webHookPublisher = webHookPublisher;
        this.startableForPlugins = startableForPlugins;
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
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
        webHookPublisher.register(getPluginKey(), eventIdentifier, url);

        if ("plugin_enabled".equals(eventIdentifier))
        {
            startableForPlugins.register(getPluginKey(), new Runnable()
            {
                @Override
                public void run()
                {
                    final String baseUrl = WebHookModuleDescriptor.this.applicationProperties.getBaseUrl();
                    webHookPublisher.publish(eventIdentifier, EventMatcher.ALWAYS_TRUE,
                            EventSerializers.forMap(null, ImmutableMap.<String, Object>of(
                                    "key", getPluginKey(),
                                    "serverKey", WebHookModuleDescriptor.this.consumerService.getConsumer().getKey(),
                                    "baseurl", (baseUrl != null ? baseUrl : ""),
                                    "baseUrl", (baseUrl != null ? baseUrl : ""))));
                }
            });
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        webHookPublisher.unregister(getPluginKey(), eventIdentifier, url);
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
