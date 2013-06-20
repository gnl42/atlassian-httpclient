package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;

public interface PublishTaskFactory
{
    PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookListener listener);
}
