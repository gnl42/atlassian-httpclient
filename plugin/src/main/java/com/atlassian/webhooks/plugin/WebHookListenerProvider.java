package com.atlassian.webhooks.plugin;

import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;

/**
 * Provides WebHook listeners to which serialized event may be fired. {@link com.atlassian.webhooks.spi.provider.EventMatcher}
 * are executed on the provided listeners before HttpClient posts the event to the path returned by {@link WebHookListener}.
 */
public interface WebHookListenerProvider
{
    Iterable<WebHookListener> getListeners(final WebHookEvent webHookEvent);

}
