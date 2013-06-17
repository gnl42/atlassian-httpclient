package com.atlassian.webhooks.spi.provider;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public interface WebHookListenerActionValidator
{
    MessageCollection validateWebHookAddition(WebHookListenerRegistrationParameters registrationParameters);

    MessageCollection validateWebHookUpdate(WebHookListenerRegistrationParameters registrationParameters);

    MessageCollection validateWebHookDeletion(WebHookListenerRegistrationParameters registrationParameters);

    static final class DefaultMessageCollection implements MessageCollection
    {
        private List<Message> messages = Lists.newArrayList();

        public DefaultMessageCollection(Message defaultMessage)
        {
            addMessage(defaultMessage);
        }

        public DefaultMessageCollection()
        {
        }

        @Override
        public void addMessage(String key, Serializable... arguments)
        {
            addMessage(new DefaultMessage(key, arguments));
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

    static final class DefaultMessage implements Message
    {
        private final String key;
        private final Serializable[] arguments;

        public DefaultMessage(String key, Serializable[] arguments)
        {
            this.key = key;
            this.arguments = arguments;
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public Serializable[] getArguments()
        {
            return arguments;
        }
    }
}
