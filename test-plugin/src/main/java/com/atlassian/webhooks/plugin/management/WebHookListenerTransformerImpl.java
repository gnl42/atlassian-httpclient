package com.atlassian.webhooks.plugin.management;

import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Optional;

import java.net.URI;

/**
 */
public class WebHookListenerTransformerImpl implements WebHookListenerTransformer
{
    @Override
    public Optional<WebHookListener> transform(final WebHookListenerParameters webHookListenerParameters)
    {
        return Optional.<WebHookListener>of(new WebHookListener()
        {
            @Override
            public String getPluginKey()
            {
                return "com.atlassian.webhooks.atlassian-webhooks-test-plugin";
            }

            @Override
            public URI getPath()
            {
                return URI.create(webHookListenerParameters.getUrl());
            }

            @Override
            public Object getListenerParameters()
            {
                final String parameters = webHookListenerParameters.getParameters();
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
        });
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
