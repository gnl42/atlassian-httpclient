package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface WebHookConsumer extends IdentifiableWebHookConsumer
{
    URI getPath();
}
