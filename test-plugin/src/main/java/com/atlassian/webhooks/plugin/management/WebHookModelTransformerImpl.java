package com.atlassian.webhooks.plugin.management;

import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookModelTransformer;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

import java.net.URI;

/**
 */
public class WebHookModelTransformerImpl implements WebHookModelTransformer
{
    @Override
    public WebHookConsumer transform(final WebHookListenerRegistrationParameters webHookConsumerModel)
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
                final String parameters = webHookConsumerModel.getParameters();
                if (parameters.contains(":"))
                {
                    return new RefAppListenerParameters(parameters.substring(0, parameters.indexOf(':')), parameters.substring(parameters.indexOf(':') + 1));
                }
                else
                {
                    return parameters;
                }
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

        public RefAppListenerParameters(String qualificator, String secondaryKey)
        {
            this.qualificator = qualificator;
            this.secondaryKey = secondaryKey;
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
