package com.atlassian.webhooks.plugin.impl;

import com.atlassian.httpclient.api.Request;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.beans.factory.DisposableBean;

/**
 * Signer implementation which tries to first get an impl from an OSGi service
 */
public final class RequestSignerImpl implements RequestSigner, DisposableBean
{
    private final ServiceTracker serviceTracker;
    private static final RequestSigner NO_OP_REQUEST_SIGNER = new RequestSigner()
    {
        @Override
        public void sign(String pluginKey, Request request)
        {
            // do nothing
        }
    };

    private volatile RequestSigner delegate = NO_OP_REQUEST_SIGNER;

    public RequestSignerImpl(final BundleContext bundleContext)
    {
        this.serviceTracker = new ServiceTracker(bundleContext, RequestSigner.class.getName(), new ServiceTrackerCustomizer()
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                RequestSigner signer = (RequestSigner) bundleContext.getService(reference);
                delegate = signer;
                return signer;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service)
            {
                delegate = (RequestSigner) service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
                delegate = NO_OP_REQUEST_SIGNER;
            }
        });
        serviceTracker.open();
    }

    @Override
    public void sign(String pluginKey, Request request)
    {
        delegate.sign(pluginKey, request);
    }

    @Override
    public void destroy() throws Exception
    {
        serviceTracker.close();
    }

}
