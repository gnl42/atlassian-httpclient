package com.atlassian.webhooks.plugin.rest;


import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookConsumerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookConsumerActionValidatorImpl implements WebHookConsumerActionValidator
{
    private final BundleContext bundleContext;

    public WebHookConsumerActionValidatorImpl(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public MessageCollection validateWebHookAddition(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookConsumerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookConsumerActionValidator webHookConsumerActionValidator = (WebHookConsumerActionValidator) bundleContext.getService(serviceReference);
                return webHookConsumerActionValidator.validateWebHookAddition(registrationParameters);
            }
            finally
            {
                bundleContext.ungetService(serviceReference);
            }
        }
        else
        {
            return new DefaultMessageCollection();
        }
    }

    @Override
    public MessageCollection validateWebHookDeletion(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookConsumerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookConsumerActionValidator webHookConsumerActionValidator = (WebHookConsumerActionValidator) bundleContext.getService(serviceReference);
                return webHookConsumerActionValidator.validateWebHookDeletion(registrationParameters);
            }
            finally
            {
                bundleContext.ungetService(serviceReference);
            }
        }
        else
        {
            return new DefaultMessageCollection();
        }
    }

    @Override
    public MessageCollection validateWebHookUpdate(WebHookListenerRegistrationParameters registrationParameters)
    {
        ServiceReference serviceReference = bundleContext.getServiceReference(WebHookConsumerActionValidator.class.getName());
        if (serviceReference != null)
        {
            try
            {
                WebHookConsumerActionValidator webHookConsumerActionValidator = (WebHookConsumerActionValidator) bundleContext.getService(serviceReference);
                return webHookConsumerActionValidator.validateWebHookUpdate(registrationParameters);
            }
            finally
            {
                bundleContext.ungetService(serviceReference);
            }
        }
        else
        {
            return new DefaultMessageCollection();
        }
    }
}
