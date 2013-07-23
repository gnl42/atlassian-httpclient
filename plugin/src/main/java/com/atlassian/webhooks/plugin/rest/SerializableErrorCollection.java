package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class SerializableErrorCollection
{
    private final MessageCollection messageCollection;

    public SerializableErrorCollection(Exception e)
    {
        this(new WebHookListenerActionValidator.ErrorMessageCollection(e.getMessage()));
    }

    public SerializableErrorCollection(MessageCollection messageCollection)
    {
        this.messageCollection = messageCollection;
    }

    @XmlElement
    public List<Message> getMessages()
    {
        return Lists.newArrayList(Iterables.transform(messageCollection.getMessages(), new Function<Message, Message>()
        {
            @Override
            public Message apply(Message message)
            {
                return new SerializableMessage(message);
            }
        }));
    }

    @XmlRootElement
    public static class SerializableMessage implements Message
    {

        private final Message message;

        public SerializableMessage(final Message message)
        {
            this.message = message;
        }

        @XmlElement
        @Override
        public String getKey()
        {
            return message.getKey();
        }

        @XmlElement
        @Override
        public Serializable[] getArguments()
        {
            return message.getArguments();
        }
    }
}
