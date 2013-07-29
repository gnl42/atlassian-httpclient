package com.atlassian.webhooks.plugin.rest;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Introduced in order not to break JIRA WEBHOOK API, by changing REST input parameters. Please, don't add anything
 * here, legacy is legacy, new special parameters are supposed to be passed to the REST resource as parameters.
 * @deprecated in JIRA 7.0 we will break API and change filter and excludeIssueDetails to be a part of parameters.
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
            final Map<String, Object> parameters = createJiraParameters(adaptedRegistrationParameters);
            return new WebHookListenerRegistration(adaptedRegistrationParameters.name, adaptedRegistrationParameters.url, parameters, Lists.newArrayList(adaptedRegistrationParameters.events), adaptedRegistrationParameters.enabled);
        }
        else
        {
            return new WebHookListenerRegistration(adaptedRegistrationParameters.name, adaptedRegistrationParameters.url, adaptedRegistrationParameters.parameters, Lists.newArrayList(adaptedRegistrationParameters.events), adaptedRegistrationParameters.enabled);
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
                    (registration.getParameters().get("excludeIssueDetails") != null ||
                            registration.getParameters().get("filter") != null))
            {
                Optional<String> filter = JiraParametersParser.getFilter(registration.getParameters());
                boolean excludeIssueDetails = JiraParametersParser.getExcludeIssueDetails(registration.getParameters());
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

    private Map<String, Object> createJiraParameters(AdaptedRegistrationParameters registrationParameters)
    {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("excludeIssueDetails", registrationParameters.excludeIssueDetails);
        if (registrationParameters.filter != null)
        {
            builder.put("filter", registrationParameters.filter);
        }
        return builder.build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AdaptedRegistrationParameters
    {
        @XmlAttribute
        public String name;
        @XmlAttribute
        public String[] events;
        @XmlAttribute
        public Map<String, Object> parameters;
        @XmlAttribute
        public String url;
        @XmlAttribute
        public String filter;
        @XmlAttribute
        public Boolean excludeIssueDetails;
        @XmlAttribute
        public Boolean enabled;
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
    }

    public static class JiraParametersParser
    {
        public static Boolean getExcludeIssueDetails(Map<String, Object> parameters)
        {
            return Objects.firstNonNull((Boolean) parameters.get("excludeIssueDetails"), false);
        }

        public static Optional<String> getFilter(Map<String, Object> parameters)
        {
            return Optional.fromNullable((String) parameters.get("filter"));
        }
    }

}
