package com.atlassian.webhooks.plugin.rest;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.net.URI;

/**
 * This class is introduced in order not to break JIRA WEBHOOK API, by changing REST input parameters. Please, don't add anything
 * here, legacy is legacy, new special parameters are supposed to be passed to the REST resource as parameters.
 */
public class RegistrationParametersAdapter extends XmlAdapter<RegistrationParametersAdapter.AdaptedRegistrationParameters, WebHookListenerRegistration>
{
    @Override
    public WebHookListenerRegistration unmarshal(final AdaptedRegistrationParameters adaptedRegistrationParameters) throws Exception
    {
        if (adaptedRegistrationParameters == null)
        {
            throw new IllegalArgumentException("Illegal arguments passed during WebHookListener registration.");
        }
        if (adaptedRegistrationParameters.filter != null || adaptedRegistrationParameters.excludeIssueDetails != null)
        {
            final String parameters = createJiraParameters(adaptedRegistrationParameters);
            return new WebHookListenerRegistration(adaptedRegistrationParameters.name, adaptedRegistrationParameters.url, parameters, Lists.newArrayList(adaptedRegistrationParameters.events));
        }
        else
        {
            return new WebHookListenerRegistration(adaptedRegistrationParameters.name, adaptedRegistrationParameters.url, adaptedRegistrationParameters.parameters, Lists.newArrayList(adaptedRegistrationParameters.events));
        }
    }

    @Override
    public AdaptedRegistrationParameters marshal(WebHookListenerRegistration registration) throws Exception
    {
        if (registration instanceof WebHookListenerRegistrationResponse)
        {
            final WebHookListenerRegistrationResponse webHookListenerRegistration = (WebHookListenerRegistrationResponse) registration;
            final AdaptedRegistrationResponse response = new AdaptedRegistrationResponse();
            // this is JIRA WebHook Response
            if (registration.getParameters() != null &&
                    (registration.getParameters().contains("excludeIssueDetails") || registration.getParameters().contains("filter")))
            {
                Optional<String> filter = ParametersParser.getFilter(registration.getParameters());
                boolean excludeIssueDetails = ParametersParser.getExcludeIssueDetails(registration.getParameters());
                response.filter = filter.orNull();
                response.excludeIssueDetails = excludeIssueDetails;
            }
            else
            {
                response.parameters = registration.getParameters();
            }
            response.events = Iterables.toArray(webHookListenerRegistration.getEvents(), String.class);
            response.name = webHookListenerRegistration.getName();
            response.url = webHookListenerRegistration.getUrl();
            response.self = webHookListenerRegistration.getSelf();
            response.enabled = webHookListenerRegistration.isEnabled();
            response.lastUpdated = webHookListenerRegistration.getLastUpdated();
            response.lastUpdatedDisplayName = webHookListenerRegistration.getLastUpdatedDisplayName();
            response.lastUpdatedUser = webHookListenerRegistration.getLastUpdatedUser();
            return response;
        }
        else
        {
            throw new IllegalArgumentException("RegistrationResource is supposed to always return an instance of WebHookListenerRegistrationResponse");
        }
    }

    private String createJiraParameters(AdaptedRegistrationParameters registrationParameters)
    {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("excludeIssueDetails", registrationParameters.excludeIssueDetails.toString());
        if (registrationParameters.filter != null)
        {
            builder.put("filter", registrationParameters.filter);
        }
        return new JSONObject(builder.build()).toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AdaptedRegistrationParameters
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
        public String filter;
        @XmlAttribute
        public Boolean excludeIssueDetails;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AdaptedRegistrationResponse extends AdaptedRegistrationParameters
    {
        @XmlAttribute
        public URI self;
        @XmlAttribute
        public String lastUpdatedUser;
        @XmlAttribute
        public String lastUpdatedDisplayName;
        @XmlAttribute
        public Long lastUpdated;
        @XmlAttribute
        public boolean enabled;
    }

    public static class ParametersParser
    {
        private static final Logger logger = LoggerFactory.getLogger(ParametersParser.class);

        public static boolean getExcludeIssueDetails(String parameters)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(parameters);
                return jsonObject.optBoolean("excludeIssueDetails");
            }
            catch (JSONException e)
            {
                logger.error("Couldn't parse WebHookListener parameters. Parameters are probably not in JSON format [{}]", parameters);
                throw new IllegalArgumentException("Couldn't parse WebHookListener parameters", e);
            }
        }

        public static Optional<String> getFilter(String parameters)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(parameters);
                return Optional.fromNullable(jsonObject.optString("filter"));
            }
            catch (JSONException e)
            {
                logger.error("Couldn't parse WebHookListener parameters. Parameters are probably not in JSON format [{}]", parameters);
                throw new RuntimeException(e);
            }
        }
    }

}
