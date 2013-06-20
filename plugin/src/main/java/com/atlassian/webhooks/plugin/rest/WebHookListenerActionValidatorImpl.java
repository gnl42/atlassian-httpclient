package com.atlassian.webhooks.plugin.rest;


import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookListenerActionValidatorImpl implements WebHookListenerActionValidator
{
    private final BundleContext bundleContext;

    public WebHookListenerActionValidatorImpl(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public MessageCollection validateWebHookAddition(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookListenerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookListenerActionValidator webHookListenerActionValidator = (WebHookListenerActionValidator) bundleContext.getService(serviceReference);
                return webHookListenerActionValidator.validateWebHookAddition(registrationParameters);
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

    @Override
    public MessageCollection validateWebHookDeletion(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookListenerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookListenerActionValidator webHookListenerActionValidator = (WebHookListenerActionValidator) bundleContext.getService(serviceReference);
                return webHookListenerActionValidator.validateWebHookDeletion(registrationParameters);
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

    @Override
    public MessageCollection validateWebHookUpdate(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookListenerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookListenerActionValidator webHookListenerActionValidator = (WebHookListenerActionValidator) bundleContext.getService(serviceReference);
                return webHookListenerActionValidator.validateWebHookUpdate(registrationParameters);
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
