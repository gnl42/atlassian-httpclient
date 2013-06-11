package com.atlassian.webhooks.spi.provider;

public interface WebHookModelTransformer
{

    WebHookConsumer transform(WebHookRegistrationParameters webHookConsumerModel);

}
