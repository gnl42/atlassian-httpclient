package com.atlassian.webhooks.plugin.management;

import com.atlassian.webhooks.spi.provider.WebHookListener;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerTransformer;
import com.google.common.base.Optional;

import java.net.URI;
import java.util.Map;

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
                final Map<String, Object> parameters = webHookListenerParameters.getParameters();
                if (parameters.containsKey("qualification") && parameters.containsKey("secondaryKey"))
                {
                    final String qualificator = ((Boolean) parameters.get("qualification")).toString();
                    final String secondaryKey = (String) parameters.get("secondaryKey");
                    return new RefAppListenerParameters(qualificator, secondaryKey);
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
