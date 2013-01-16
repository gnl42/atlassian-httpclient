package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;

public interface PublishTaskFactory
{
    PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookConsumer consumer);
}
