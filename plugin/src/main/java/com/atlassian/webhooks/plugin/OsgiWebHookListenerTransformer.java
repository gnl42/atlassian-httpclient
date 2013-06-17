package com.atlassian.webhooks.plugin;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

import static com.google.common.base.Preconditions.checkNotNull;

public class OsgiWebHookListenerTransformer implements WebHookListenerTransformer
{
    private WebHookListenerTransformer webHookListenerTransformer;

    public OsgiWebHookListenerTransformer(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookListenerTransformer.class, new WebHookModelTransformerWaitableServiceTrackerCustomizer());
    }

    @Override
    public WebHookListener transform(final WebHookListenerRegistrationParameters webHookListenerModel)
    {
        return webHookListenerTransformer.transform(webHookListenerModel);
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
