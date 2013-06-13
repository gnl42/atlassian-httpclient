package com.atlassian.webhooks.plugin.rest;


import com.atlassian.jira.i18n.DefaultMessage;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookConsumerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookRegistrationParameters;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookConsumerActionValidatorImpl implements WebHookConsumerActionValidator
{
    private final BundleContext bundleContext;

    public WebHookConsumerActionValidatorImpl(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public MessageCollection validateWebHookAddition(WebHookRegistrationParameters registrationParameters)
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
    public MessageCollection validateWebHookDeletion(WebHookRegistrationParameters registrationParameters)
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
    public MessageCollection validateWebHookUpdate(WebHookRegistrationParameters registrationParameters)
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
