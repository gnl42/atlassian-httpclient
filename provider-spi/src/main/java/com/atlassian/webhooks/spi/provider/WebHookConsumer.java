package com.atlassian.webhooks.spi.provider;

import java.net.URI;

public interface WebHookConsumer
{
    ConsumerKey getConsumerKey();

    URI getPath();
}
