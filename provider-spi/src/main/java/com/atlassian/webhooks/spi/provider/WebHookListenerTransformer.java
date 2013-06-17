package com.atlassian.webhooks.spi.provider;

public interface WebHookListenerTransformer
{

    WebHookListener transform(WebHookListenerRegistrationParameters webHookConsumerModel);

}
