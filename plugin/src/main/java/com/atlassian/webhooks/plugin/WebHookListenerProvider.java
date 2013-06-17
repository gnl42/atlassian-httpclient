package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookEvent;

public interface WebHookListenerProvider
{

    Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent);

}
