package com.atlassian.webhooks.plugin;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Finds the delegates transformation to {@link WebHookListenerTransformer} found in the bundleContext.
 */
public class OsgiWebHookListenerTransformer implements WebHookListenerTransformer
{
    private WebHookListenerTransformer webHookListenerTransformer;

    public OsgiWebHookListenerTransformer(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookListenerTransformer.class, new WebHookModelTransformerWaitableServiceTrackerCustomizer());
    }

    @Override
    public Optional<WebHookListener> transform(final WebHookListenerParameters webHookListenerParameters)
    {
        return webHookListenerTransformer.transform(webHookListenerParameters);
    }

    private final class WebHookModelTransformerWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookListenerTransformer>
    {

        @Override
        public WebHookListenerTransformer adding(WebHookListenerTransformer webHookListenerTransformer)
        {
            if (OsgiWebHookListenerTransformer.this.webHookListenerTransformer != null && !webHookListenerTransformer.getClass().equals(OsgiWebHookListenerTransformer.class))
            {
                throw new IllegalStateException("There can be only one implementation of WebHookListenerTransformer in the system. Found [ " + webHookListenerTransformer.getClass() + ", " + OsgiWebHookListenerTransformer.this.webHookListenerTransformer.getClass() + "]");
            }
            // ensure we don't create a reference to self.
            if (!webHookListenerTransformer.getClass().equals(OsgiWebHookListenerTransformer.class))
            {
                OsgiWebHookListenerTransformer.this.webHookListenerTransformer = webHookListenerTransformer;
            }
            return webHookListenerTransformer;
        }

        @Override
        public void removed(WebHookListenerTransformer webHookListenerTransformer)
        {
            OsgiWebHookListenerTransformer.this.webHookListenerTransformer = null;
        }
    }
}
