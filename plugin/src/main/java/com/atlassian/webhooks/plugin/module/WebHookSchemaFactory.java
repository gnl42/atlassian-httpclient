package com.atlassian.webhooks.plugin.module;

import com.atlassian.plugin.schema.spi.SchemaTransformer;
import com.atlassian.webhooks.plugin.WebHookRegistry;
import org.dom4j.Document;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.*;

/**
 * Registers a web hook module descriptor factory
 */
public final class WebHookSchemaFactory implements SchemaTransformer
{
    private final WebHookRegistry webHookRegistry;

    public WebHookSchemaFactory(WebHookRegistry webHookRegistry)
    {
        this.webHookRegistry = checkNotNull(webHookRegistry);
    }

    @Override
    public Document transform(Document document)
    {
        final Element parent = (Element) document.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");
        for (String id : webHookRegistry.getWebHookIds())
        {
            parent.addElement("xs:enumeration").addAttribute("value", id);
        }
        return document;
    }
}
