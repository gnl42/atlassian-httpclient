package com.atlassian.webhooks.plugin.web;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
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
    private static final String WEBHOOKS_ADMIN_TEMPLATE_PATH = "templates/webhooks-admin.vm";
    private static final String NO_ADMIN_PRIVILEGES_TEMPLATE_PATH = "templates/no_admin_privileges.vm";
    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";

    private final TemplateRenderer templateRenderer;
    protected final WebResourceManager webResourceManager;
    private final I18nResolver i18nResolver;
    private final WebHookUIRegistry webHookUIRegistry;
    private final UserManager userManager;
    private final WebSudoManager webSudoManager;

    public WebHookAdminServlet(
            TemplateRenderer templateRenderer,
            WebResourceManager webResourceManager,
            I18nResolver i18nResolver,
            WebHookUIRegistry webHookUIRegistry,
            UserManager userManager,
            WebSudoManager webSudoManager)
    {
        this.webSudoManager = webSudoManager;
        this.webHookUIRegistry = checkNotNull(webHookUIRegistry);
        this.userManager = checkNotNull(userManager);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceManager = checkNotNull(webResourceManager);
        this.i18nResolver = checkNotNull(i18nResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(req);
            if (userManager.isAdmin(userManager.getRemoteUsername()) || userManager.isSystemAdmin(userManager.getRemoteUsername()))
            {
                render(resp);
            }
            else
            {
                renderNoAdminPrivileges(resp);
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    private void renderNoAdminPrivileges(HttpServletResponse response) throws IOException
    {
        response.setContentType(TEXT_HTML_CHARSET_UTF_8);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        templateRenderer.render(NO_ADMIN_PRIVILEGES_TEMPLATE_PATH, response.getWriter());
    }

    private void render(HttpServletResponse response) throws IOException
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
        response.setContentType(TEXT_HTML_CHARSET_UTF_8);
        templateRenderer.render(WEBHOOKS_ADMIN_TEMPLATE_PATH, builder.build(), response.getWriter());
    }

}
