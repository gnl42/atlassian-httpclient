package com.atlassian.webhooks.plugin.rest;

import com.atlassian.webhooks.spi.provider.WebHookRegistrationParameters;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true) // so we can send back the RegistrationResponse
public class Registration implements WebHookRegistrationParameters
{
    @XmlElement
	private String name;

    @XmlElement
	private String url;

	@XmlElement
	private String events;

	@XmlElement
	private String parameters;

    @SuppressWarnings ("UnusedDeclaration")
    public Registration() {
	}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String getEvents()
    {
        return events;
    }

    public void setEvents(String events)
    {
        this.events = events;
    }

    @Override
    public String getParameters()
    {
        return parameters;
    }

    public void setParameters(String parameters)
    {
        this.parameters = parameters;
    }
}
