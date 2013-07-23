package com.atlassian.webhooks.api.provider;

import com.atlassian.annotations.PublicApi;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Optional;

import static com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator.ErrorMessageCollection.emptyErrorMessageCollection;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@PublicApi
public class WebHookListenerServiceResponse
{
    private final MessageCollection messageCollection;
    private final Optional<WebHookListenerParameters> registeredListener;

    public WebHookListenerServiceResponse(MessageCollection messageCollection)
    {
        this(messageCollection, Optional.<WebHookListenerParameters>absent());
    }

    public WebHookListenerServiceResponse(WebHookListenerParameters webHookListenerParameters)
    {
        this(emptyErrorMessageCollection(), fromNullable(webHookListenerParameters));
    }

    private WebHookListenerServiceResponse(MessageCollection messageCollection,
            Optional<WebHookListenerParameters> webHookListenerParameters)
    {
        this.messageCollection = checkNotNull(messageCollection);
        this.registeredListener = webHookListenerParameters;
    }

    public MessageCollection getMessageCollection()
    {
        return messageCollection;
    }

    public Optional<WebHookListenerParameters> getRegisteredListener()
    {
        return registeredListener;
    }
}
