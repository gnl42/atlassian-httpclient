package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * This interface should be implemented by the provider which wants to validate the parameters of WebHook listener before
 * registration/update or removal.
 * There can be only one WebHookListenerActionValidator per product.
 */
@PublicSpi
public interface WebHookListenerActionValidator
{
    /**
     * Validates the parameters of newly registered WebHook listener. If this method returns any errors, WebHook listener
     * will not be registered.
     *
     * @param registrationParameters Parameters of WebHook listener.
     * @return Collection with errors, if the validation failed. Empty collection if the validation passed.
     */
    MessageCollection validateWebHookRegistration(WebHookListenerRegistrationParameters registrationParameters);

    /**
     * Validates the parameters of WebHook listener before the update. If this method returns any errors, WebHook listener
     * will not be updated.
     *
     * @param registrationParameters Parameters of WebHook listener.
     * @return Collection with errors, if the validation failed. Empty collection if the validation passed.
     */
    MessageCollection validateWebHookUpdate(WebHookListenerRegistrationParameters registrationParameters);

    /**
     * Validates the parameters of WebHook listener before the removal. If this method returns any errors, WebHook listener
     * will not be deleted.
     *
     * @param registrationParameters Parameters of WebHook listener.
     * @return Collection with errors, if the validation failed. Empty collection if the validation passed.
     */
    MessageCollection validateWebHookRemoval(WebHookListenerParameters registrationParameters);

    static final class ErrorMessageCollection implements MessageCollection
    {
        private List<Message> messages;

        public static ErrorMessageCollection emptyErrorMessageCollection()
        {
            return new ErrorMessageCollection(ImmutableList.<Message>of());
        }

        private ErrorMessageCollection(List<Message> messages)
        {
            this.messages = messages;
        }

        public ErrorMessageCollection()
        {
            this(Lists.<Message>newArrayList());
        }

        public ErrorMessageCollection(Message defaultMessage)
        {
            this();
            addMessage(defaultMessage);
        }

        public ErrorMessageCollection(String message)
        {
            this(new ErrorMessage(message));
        }

        @Override
        public void addMessage(String key, Serializable... arguments)
        {
            addMessage(new ErrorMessage(key, arguments));
        }

        @Override
        public void addMessage(Message message)
        {
            messages.add(message);
        }

        @Override
        public void addAll(List<Message> errorMessages)
        {
            this.messages.addAll(errorMessages);
        }

        @Override
        public boolean isEmpty()
        {
            return messages.isEmpty();
        }

        @Override
        public List<Message> getMessages()
        {
            return messages;
        }
    }

    static final class ErrorMessage implements Message
    {
        private final String message;
        private final Serializable[] arguments;

        public ErrorMessage(String message, Serializable[] arguments)
        {
            this.message = message;
            this.arguments = arguments;
        }

        public ErrorMessage(String message)
        {
            this(message, null);
        }

        @Override
        public String getKey()
        {
            return message;
        }

        @Override
        public Serializable[] getArguments()
        {
            return arguments;
        }
    }
}
