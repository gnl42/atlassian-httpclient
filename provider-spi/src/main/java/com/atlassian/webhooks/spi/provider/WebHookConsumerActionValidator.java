package com.atlassian.webhooks.spi.provider;

import java.util.List;

public interface WebHookConsumerActionValidator
{
    interface MessageCollection
    {

        boolean isEmpty();

        List<String> getMessages();
    }


    MessageCollection validateWebHookAddition(WebHookRegistrationParameters registrationParameters);

    MessageCollection validateWebHookUpdate(WebHookRegistrationParameters registrationParameters);

    MessageCollection validateWebHookDeletion(WebHookRegistrationParameters registrationParameters);
}
