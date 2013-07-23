package com.atlassian.webhooks.plugin.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;

public class WebHookEventDispatcher
{
    private final EventPublisher eventPublisher;

    public WebHookEventDispatcher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public void webHookCreated(WebHookListenerParameters webHook)
    {
        eventPublisher.publish(
                new WebHookCreatedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookDeleted(WebHookListenerParameters webHook)
    {
        eventPublisher.publish(
                new WebHookDeletedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookEdited(WebHookListenerParameters webHook)
    {
        eventPublisher.publish(
                new WebHookEditedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookEnabled(WebHookListenerParameters webHook)
    {
        eventPublisher.publish(
                new WebHookEnabledEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookDisabled(WebHookListenerParameters webHook)
    {
        eventPublisher.publish(
                new WebHookDisabledEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }
}
