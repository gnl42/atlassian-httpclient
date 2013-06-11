package com.atlassian.webhooks.plugin;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookModelTransformer;
import com.atlassian.webhooks.spi.provider.WebHookRegistrationParameters;

import static com.google.common.base.Preconditions.checkNotNull;

public class OsgiWebHookConsumerTransformer implements WebHookModelTransformer
{
    private WebHookModelTransformer webHookModelTransformer;

    public OsgiWebHookConsumerTransformer(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookModelTransformer.class, new WebHookModelTransformerWaitableServiceTrackerCustomizer());
    }

    @Override
    public WebHookConsumer transform(final WebHookRegistrationParameters webHookConsumerModel)
    {
        return webHookModelTransformer.transform(webHookConsumerModel);
    }

    private final class WebHookModelTransformerWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookModelTransformer>
    {

        @Override
        public WebHookModelTransformer adding(WebHookModelTransformer webHookModelTransformer)
        {
            if (OsgiWebHookConsumerTransformer.this.webHookModelTransformer != null)
            {
                throw new RuntimeException("There can be only one implementation of WebHookModelTransformer in the system. Found [ " + webHookModelTransformer.getClass() + ", " + OsgiWebHookConsumerTransformer.this.webHookModelTransformer.getClass() + "]");
            }
            OsgiWebHookConsumerTransformer.this.webHookModelTransformer = webHookModelTransformer;
            return webHookModelTransformer;
        }

        @Override
        public void removed(WebHookModelTransformer webHookModelTransformer)
        {
            OsgiWebHookConsumerTransformer.this.webHookModelTransformer = null;
        }
    }
}
