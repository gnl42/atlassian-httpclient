package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SerializableErrorCollection
{
    private final MessageCollection messageCollection;

    public SerializableErrorCollection(MessageCollection messageCollection)
    {
        this.messageCollection = messageCollection;
    }

    @XmlElement
    public List<String> getMessages()
    {
        return Lists.newArrayList(Iterables.transform(messageCollection.getMessages(), new Function<Message, String>()
        {
            @Override
            public String apply(Message message)
            {
                return message.getKey();
            }
        }));
    }
}
