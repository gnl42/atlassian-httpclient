package com.atlassian.webhooks.spi.provider;

import com.atlassian.sal.api.message.MessageCollection;

public interface WebHookConsumerActionValidator
{
    MessageCollection validateWebHookAddition(WebHookRegistrationParameters registrationParameters);

    MessageCollection validateWebHookUpdate(WebHookRegistrationParameters registrationParameters);

    MessageCollection validateWebHookDeletion(WebHookRegistrationParameters registrationParameters);
}
