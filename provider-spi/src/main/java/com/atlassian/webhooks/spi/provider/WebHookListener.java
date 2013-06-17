package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface WebHookListener
{
    String getPluginKey();

    URI getPath();

    Object getListenerParameters();

    /**
     * This method gives consumers opportunity to modify the serialized json according to consumer custom parameters.
     *
     * @param json - serialized json of the event.
     * @return final POST body, which will be accepted by client.
     */
    String getConsumableBodyJson(String json);
}