package com.atlassian.webhooks.plugin;

public interface PublishTaskFactory
{
    PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookConsumer consumer);
}
