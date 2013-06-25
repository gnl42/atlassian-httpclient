package com.atlassian.webhooks.plugin.web;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.plugin.web.util.HtmlSafeContent;
import com.atlassian.webhooks.plugin.web.util.RendererContextBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookAdminServlet extends HttpServlet
{
    private static final String TEMPLATE_PATH = "templates/webhooks-admin.vm";

    private final TemplateRenderer templateRenderer;
    protected final WebResourceManager webResourceManager;
    private final I18nResolver i18nResolver;
    private final WebHookUIRegistry webHookUIRegistry;

    public WebHookAdminServlet(TemplateRenderer templateRenderer, WebResourceManager webResourceManager, I18nResolver i18nResolver, WebHookUIRegistry webHookUIRegistry)
    {
        this.webHookUIRegistry = webHookUIRegistry;
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceManager = checkNotNull(webResourceManager);
        this.i18nResolver = checkNotNull(i18nResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        render(req, resp);
//        super.doGet(req, resp);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void render(HttpServletRequest req, HttpServletResponse response) throws IOException
    {
        webResourceManager.requireResourcesForContext("atl.plugins.webhooks.admin");
        final RendererContextBuilder builder = new RendererContextBuilder()
                .put("i18n", i18nResolver)
                .put("webResources", new HtmlSafeContent()
                {
                    public CharSequence get()
                    {
                        StringWriter writer = new StringWriter();
                        webResourceManager.includeResources(writer);
                        return writer.toString();
                    }
                })
                .put("webResourceManager", webResourceManager)
                .put("webHookUIRegistry", webHookUIRegistry);
        response.setContentType("text/html; charset=utf-8");
        templateRenderer.render(TEMPLATE_PATH, builder.build(), response.getWriter());
    }

}
