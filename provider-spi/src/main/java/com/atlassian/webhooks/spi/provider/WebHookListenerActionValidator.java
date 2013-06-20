package com.atlassian.webhooks.spi.provider;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public interface WebHookListenerActionValidator
{
    MessageCollection validateWebHookAddition(WebHookListenerRegistrationParameters registrationParameters);

    MessageCollection validateWebHookUpdate(WebHookListenerRegistrationParameters registrationParameters);

    MessageCollection validateWebHookDeletion(WebHookListenerRegistrationParameters registrationParameters);

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
        public void addAll(List<Message> messages)
        {
            messages.addAll(messages);
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
