package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.message.MessageCollection;

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
    public List<Message> getMessages()
    {
        return messageCollection.getMessages();
    }
}
