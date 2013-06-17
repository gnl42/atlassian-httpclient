package com.atlassian.webhooks.plugin.management;

import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;

import java.net.URI;

/**
 */
public class WebHookListenerTransformerImpl implements WebHookListenerTransformer
{
    @Override
    public WebHookListener transform(final WebHookListenerRegistrationParameters webHookConsumerModel)
    {
        return new WebHookListener()
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
            public Object getListenerParameters()
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
