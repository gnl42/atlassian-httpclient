package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookEvent;

public interface PublishTaskFactory
{
    PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookListener listener);
}
