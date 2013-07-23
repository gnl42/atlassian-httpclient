package com.atlassian.webhooks.plugin.rest;


import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import com.google.common.base.Function;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Retrieves the product's implementation of {@link WebHookListenerActionValidator} from the bundleContext.
 */
public class WebHookListenerActionValidatorImpl implements WebHookListenerActionValidator
{
    private final BundleContext bundleContext;

    public WebHookListenerActionValidatorImpl(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public MessageCollection validateWebHookRegistration(final WebHookListenerRegistrationParameters registrationParameters)
    {
        return doValidation(new Function<WebHookListenerActionValidator, MessageCollection>()
        {
            @Override
            public MessageCollection apply(WebHookListenerActionValidator validator)
            {
                return validator.validateWebHookRegistration(registrationParameters);
            }
        });
    }

    @Override
    public MessageCollection validateWebHookRemoval(final WebHookListenerParameters registrationParameters)
    {
        return doValidation(new Function<WebHookListenerActionValidator, MessageCollection>()
        {
            @Override
            public MessageCollection apply(WebHookListenerActionValidator validator)
            {
                return validator.validateWebHookRemoval(registrationParameters);
            }
        });
    }

    @Override
    public MessageCollection validateWebHookUpdate(final WebHookListenerRegistrationParameters registrationParameters)
    {
        return doValidation(new Function<WebHookListenerActionValidator, MessageCollection>()
        {
            @Override
            public MessageCollection apply(WebHookListenerActionValidator validator)
            {
                return validator.validateWebHookUpdate(registrationParameters);
            }
        });
    }

    private MessageCollection doValidation(Function<WebHookListenerActionValidator, MessageCollection> validationFunction)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookListenerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookListenerActionValidator webHookListenerActionValidator = (WebHookListenerActionValidator) bundleContext.getService(serviceReference);
                return validationFunction.apply(webHookListenerActionValidator);
            }
            finally
            {
                bundleContext.ungetService(serviceReference);
            }
        }
        else
        {
            return ErrorMessageCollection.emptyErrorMessageCollection();
        }
    }
}
