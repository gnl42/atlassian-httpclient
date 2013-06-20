package com.atlassian.webhooks.plugin.management;

import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

/**
 */
public class WebHookListenerActionValidatorImpl implements WebHookListenerActionValidator
{
    @Override
    public MessageCollection validateWebHookRegistration(final WebHookListenerRegistrationParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Filip's webhook"))
        {
            return new ErrorMessageCollection(new ErrorMessage("We're not adding WebHook from Filip"));
        }
        return ErrorMessageCollection.emptyErrorMessageCollection();
    }


    @Override
    public MessageCollection validateWebHookUpdate(final WebHookListenerRegistrationParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Seb's webhook"))
        {
            return new ErrorMessageCollection(new ErrorMessage("Seb is not allowed to updateWebHookListener WebHooks"));
        }
        return ErrorMessageCollection.emptyErrorMessageCollection();
    }

    @Override
    public MessageCollection validateWebHookRemoval(final WebHookListenerParameters registrationParameters)
    {
        if (registrationParameters.getName().equals("Jonathon's webhook"))
        {
            return new ErrorMessageCollection(new ErrorMessage("Jonathon's webhook are to important to remove them"));
        }
        return ErrorMessageCollection.emptyErrorMessageCollection();
    }
}
