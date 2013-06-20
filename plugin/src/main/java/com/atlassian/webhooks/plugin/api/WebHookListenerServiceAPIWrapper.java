package com.atlassian.webhooks.plugin.api;

import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.plugin.ao.DelegatingWebHookListenerParameters;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.atlassian.webhooks.plugin.service.InternalWebHookListenerService;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import static com.google.common.collect.Iterables.transform;

/**
 * Implementation of {@link WebHookListenerService}. Wraps the internal InternalWebHookListenerService and WebHookAO.
 */
public class WebHookListenerServiceAPIWrapper implements WebHookListenerService
{
    private final InternalWebHookListenerService internalWebHookListenerService;

    public WebHookListenerServiceAPIWrapper(InternalWebHookListenerService internalWebHookListenerService)
    {
        this.internalWebHookListenerService = internalWebHookListenerService;
    }

    @Override
    public Iterable<WebHookListenerParameters> getAllWebHookListeners()
    {
        return transform(internalWebHookListenerService.getAllWebHookListeners(), new Function<WebHookAO, WebHookListenerParameters>()
        {
            @Override
            public WebHookListenerParameters apply(final WebHookAO webHookAO)
            {
                return new DelegatingWebHookListenerParameters(webHookAO);
            }
        });
    }

    @Override
    public Optional<WebHookListenerParameters> getWebHookListener(Integer id)
    {
        Optional<WebHookAO> webHook = internalWebHookListenerService.getWebHookListener(id);
        return webHook.isPresent() ? Optional.<WebHookListenerParameters>of(new DelegatingWebHookListenerParameters(webHook.get())) : Optional.<WebHookListenerParameters>absent();
    }

    @Override
    public WebHookListenerParameters registerWebHookListener(WebHookListenerParameters webHookListenerParameters)
    {
        return new DelegatingWebHookListenerParameters(internalWebHookListenerService.addWebHookListener(webHookListenerParameters.getName(),
                webHookListenerParameters.getUrl(),
                webHookListenerParameters.getEvents(),
                webHookListenerParameters.getParameters(),
                WebHookListenerManager.WebHookListenerRegistrationMethod.SERVICE));
    }

    @Override
    public WebHookListenerParameters updateWebHookListener(int id, WebHookListenerParameters webHookListenerParameters)
    {
        return new DelegatingWebHookListenerParameters(internalWebHookListenerService.updateWebHookListener(id,
                webHookListenerParameters.getName(),
                webHookListenerParameters.getUrl(),
                webHookListenerParameters.getEvents(),
                webHookListenerParameters.getParameters(),
                webHookListenerParameters.isEnabled()));
    }

    @Override
    public void deleteWebHookListener(int id)
    {
        internalWebHookListenerService.removeWebHookListener(id);
    }

    @Override
    public void clearWebHookListenerCache()
    {
        internalWebHookListenerService.clearWebHookListenerCache();
    }
}
