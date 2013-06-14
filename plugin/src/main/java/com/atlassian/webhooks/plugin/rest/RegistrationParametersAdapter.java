package com.atlassian.webhooks.plugin.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This class is introduced in order not to break JIRA API, by changing REST input parameters. Please, don't add anything
 * here, legacy is legacy, new special parameters are supposed to be passed to the REST resource as parameters.
 */
public class RegistrationParametersAdapter extends XmlAdapter<RegistrationParametersAdapter.AdapterRegistrationParameters, WebHookListenerRegistration>
{
    @Override
    public WebHookListenerRegistration unmarshal(final AdapterRegistrationParameters adapterRegistrationParameters) throws Exception
    {
        if (adapterRegistrationParameters == null)
        {
            throw new IllegalArgumentException("Illegal arguments passed during WebHookListener registration.");
        }
        if (adapterRegistrationParameters.jqlFilter != null || adapterRegistrationParameters.excludeIssueDetails != null)
        {
            final String parameters = createJiraParameters(adapterRegistrationParameters);
            return new WebHookListenerRegistration(adapterRegistrationParameters.name, adapterRegistrationParameters.url, parameters, Lists.newArrayList(adapterRegistrationParameters.events));
        }
        else
        {
            return new WebHookListenerRegistration(adapterRegistrationParameters.name, adapterRegistrationParameters.url, adapterRegistrationParameters.parameters, Lists.newArrayList(adapterRegistrationParameters.events));
        }
    }

    @Override
    public AdapterRegistrationParameters marshal(WebHookListenerRegistration registration) throws Exception
    {
        // TODO !!!
        if (registration == null)
        {
            return null;
        }
        AdapterRegistrationParameters adapterRegistrationParameters = new AdapterRegistrationParameters();
//        if (registration instanceof DefaultWebHookListenerRegistrationParameters)
//        {
//
//        }
        return null;
    }

    private String createJiraParameters(AdapterRegistrationParameters registrationParameters)
    {
        return new JSONObject(ImmutableMap.of("jqlFilter", registrationParameters.jqlFilter, "excludeIssueDetails", registrationParameters.excludeIssueDetails.toString())).toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdapterRegistrationParameters
    {
        @XmlAttribute
        public String name;
        @XmlAttribute
        public String[] events;
        @XmlAttribute
        public String parameters;
        @XmlAttribute
        public String url;
        @XmlAttribute
        public String jqlFilter;
        @XmlAttribute
        public Boolean excludeIssueDetails;
    }

}
