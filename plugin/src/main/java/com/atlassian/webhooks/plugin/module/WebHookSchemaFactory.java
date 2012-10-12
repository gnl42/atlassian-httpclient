package com.atlassian.webhooks.plugin.module;

import com.atlassian.plugin.schema.spi.SchemaTransformer;
import com.atlassian.webhooks.plugin.WebHookRegistrationManager;
import org.dom4j.Document;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registers a web hook module descriptor factory
 */
public final class WebHookSchemaFactory implements SchemaTransformer
{
    private final WebHookRegistrationManager webHookRegistrationManager;

    public WebHookSchemaFactory(WebHookRegistrationManager webHookRegistrationManager)
    {
        this.webHookRegistrationManager = checkNotNull(webHookRegistrationManager);
    }

    @Override
    public Document transform(Document document)
    {
        final Element parent = (Element) document.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");
        for (String id : webHookRegistrationManager.getIds())
        {
            parent.addElement("xs:enumeration").addAttribute("value", id);
        }
        return document;
    }
}
