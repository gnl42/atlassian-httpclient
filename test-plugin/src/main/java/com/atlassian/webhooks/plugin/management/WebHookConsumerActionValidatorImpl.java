package com.atlassian.webhooks.plugin.management;

import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookConsumerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

/**
 */
public class WebHookConsumerActionValidatorImpl implements WebHookConsumerActionValidator
{
    @Override
    public MessageCollection validateWebHookAddition(final WebHookListenerRegistrationParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Filip's webhook"))
        {
            return new DefaultMessageCollection(new DefaultMessage("We're not adding WebHook from Filip", null));
        }
        return new DefaultMessageCollection();
    }


    @Override
    public MessageCollection validateWebHookUpdate(final WebHookListenerRegistrationParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Seb's webhook"))
        {
            return new DefaultMessageCollection(new DefaultMessage("Seb is not allowed to update WebHooks", null));
        }
        return new DefaultMessageCollection();
    }

    @Override
    public MessageCollection validateWebHookDeletion(final WebHookListenerRegistrationParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Jonathon's webhook"))
        {
            return new DefaultMessageCollection(new DefaultMessage("Jonathon's webhook are to important to remove them", null));
        }
        return new DefaultMessageCollection();
    }
}
