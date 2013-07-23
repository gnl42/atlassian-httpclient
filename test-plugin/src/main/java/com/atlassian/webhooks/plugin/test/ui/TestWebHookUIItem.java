package com.atlassian.webhooks.plugin.test.ui;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.WebHookUIItem;

import java.io.IOException;
import java.io.StringWriter;

public class TestWebHookUIItem implements WebHookUIItem
{
    private static final String TEMPLATE_PATH = "templates/parameters.vm";

    private final TemplateRenderer templateRenderer;

    public TestWebHookUIItem(TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String section()
    {
        return "test-event-section";
    }

    @Override
    public Integer weight()
    {
        return 10;
    }

    @Override
    public String getHtml()
    {
        try
        {
            StringWriter writer = new StringWriter();
            templateRenderer.render(TEMPLATE_PATH, writer);
            return writer.toString();
        }
        catch (IOException e)
        {
            return "";
        }
    }
}
