package com.atlassian.webhooks.spi.provider;

import java.net.URI;

/**
 * WebHookListener encapsulates the data about a single listener registered for particular set of events.
 */
public interface WebHookListener
{
    /**
     * Returns the key of the plugin, which can operate with this listener.
     */
    String getPluginKey();

    /**
     * Returns the path to the listener.
     */
    URI getPath();

    /**
     * Returns the parameters specific for this listener (jqlFilter, includeIssueDetails, etc..).
     */
    Object getListenerParameters();

    /**
     * This method gives listeners opportunity to modify the serialized json according to consumer custom parameters.
     *
     * @param json - serialized json of the event.
     * @return final POST body, which will be accepted by client.
     */
    String getConsumableBodyJson(String json);
}
