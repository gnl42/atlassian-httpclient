package com.atlassian.webhooks.plugin.management;

import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookModelTransformer;
import com.atlassian.webhooks.spi.provider.WebHookRegistrationParameters;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.net.URI;

/**
 */
public class WebHookModelTransformerImpl implements WebHookModelTransformer
{
    @Override
    public WebHookConsumer transform(final WebHookRegistrationParameters webHookConsumerModel)
    {
        return new WebHookConsumer()
        {
            @Override
            public String getPluginKey()
            {
                return "com.atlassian.webhooks.atlassian-webhooks-test-plugin";
            }

            @Override
            public URI getPath()
            {
                return URI.create(webHookConsumerModel.getUrl());
            }

            @Override
            public Object getConsumerParams()
            {
                return new RefAppListenerParameters(Splitter.on(":").split(webHookConsumerModel.getParameters()));
            }

            @Override
            public String getConsumableBodyJson(String json)
            {
                return json;
            }
        };
    }

    public static final class RefAppListenerParameters
    {

        private final String qualificator;
        private final String secondaryKey;

        public RefAppListenerParameters(Iterable<String> parameters)
        {
            this.qualificator = Iterables.get(parameters, 0);
            this.secondaryKey = Iterables.get(parameters, 1);
        }

        public String getQualificator()
        {
            return qualificator;
        }

        public String getSecondaryKey()
        {
            return secondaryKey;
        }
    }
}
