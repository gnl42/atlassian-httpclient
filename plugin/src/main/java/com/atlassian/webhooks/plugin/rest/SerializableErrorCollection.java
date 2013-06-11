package com.atlassian.webhooks.plugin.rest;

import com.atlassian.webhooks.spi.provider.WebHookConsumerActionValidator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SerializableErrorCollection
{

    private final WebHookConsumerActionValidator.MessageCollection messageCollection;

    public SerializableErrorCollection(WebHookConsumerActionValidator.MessageCollection messageCollection)
    {
        this.messageCollection = messageCollection;
    }

    @XmlElement
    public List<String> getMessages()
    {
        return messageCollection.getMessages();
    }
}
