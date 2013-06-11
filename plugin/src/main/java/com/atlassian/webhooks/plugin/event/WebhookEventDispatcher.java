package com.atlassian.webhooks.plugin.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.webhooks.plugin.ao.WebHookAO;

public class WebHookEventDispatcher
{
    private final EventPublisher eventPublisher;

    public WebHookEventDispatcher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public void webHookCreated(WebHookAO webHook)
    {
        eventPublisher.publish(
                new WebHookAnalyticsCreatedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookDeleted(WebHookAO webHook)
    {
        eventPublisher.publish(
                new WebHookAnalyticsDeletedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookEdited(WebHookAO webHook)
    {
        eventPublisher.publish(
                new WebHookAnalyticsEditedEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookEnabled(WebHookAO webHook)
    {
        eventPublisher.publish(
                new WebHookAnalyticsEnabledEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }

    public void webHookDisabled(WebHookAO webHook)
    {
        eventPublisher.publish(
                new WebHookAnalyticsDisabledEvent(webHook.getName(), webHook.getUrl(), webHook.getEvents(), webHook.getParameters(), webHook.getRegistrationMethod())
        );
    }
}
