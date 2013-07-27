package com.atlassian.webhooks.plugin.rest;

import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.net.URI;
import java.util.Date;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RegistrationParametersAdapterTest
{

    private final RegistrationParametersAdapter parametersAdapter = new RegistrationParametersAdapter();

    @Test
    public void testCrossProductWebHookUnmarshaling() throws Exception
    {
        final RegistrationParametersAdapter.AdaptedRegistrationParameters registrationParameters = new RegistrationParametersAdapter.AdaptedRegistrationParameters();
        registrationParameters.name = "Listening for Death Star blow";
        registrationParameters.events = new String[] {"death_star_destroyed"};
        registrationParameters.url = "http://rebel-base.gov.rebel";

        WebHookListenerRegistration webHookListenerRegistration = parametersAdapter.unmarshal(registrationParameters);
        assertEquals("Listening for Death Star blow", webHookListenerRegistration.getName());
        assertEquals("http://rebel-base.gov.rebel", webHookListenerRegistration.getUrl());
        assertThat(webHookListenerRegistration.getEvents(), new ContainsEventMatcher(Sets.newHashSet("death_star_destroyed")));
    }

    @Test
    public void testJiraWebHookUnmarshaling() throws Exception
    {
        final RegistrationParametersAdapter.AdaptedRegistrationParameters registrationParameters = new RegistrationParametersAdapter.AdaptedRegistrationParameters();
        registrationParameters.name = "Listening for Death Star JIRA expectedEvents";
        registrationParameters.events = new String[] {"jira:issue_created", "jira:issue_updated"};
        registrationParameters.url = "http://rebel-base.gov.rebel";
        registrationParameters.filter = "Project = DEATH_STAR";
        registrationParameters.excludeIssueDetails = false;

        WebHookListenerRegistration webHookListenerRegistration = parametersAdapter.unmarshal(registrationParameters);
        assertEquals("Listening for Death Star JIRA expectedEvents", webHookListenerRegistration.getName());
        assertEquals("http://rebel-base.gov.rebel", webHookListenerRegistration.getUrl());

        assertThat(webHookListenerRegistration.getParameters(), containsString("Project = DEATH_STAR"));
        assertThat(webHookListenerRegistration.getEvents(), new ContainsEventMatcher(Sets.newHashSet("jira:issue_created", "jira:issue_updated")));
    }

    @Test
    public void testCrossProductWebHookMarshaling() throws Exception
    {
        final Date date = new Date();
        final WebHookListenerParameters webHookListenerParameters = new WebHookListenerParameters.WebHookListenerParametersImpl(0, true, date, "Rebel spy", "REBEL WEBHOOK",
                "http://rebel-base.gov.rebel", null, Lists.newArrayList("jira:issue_created", "jira:issue_updated"), "REST"
        );

        final WebHookListenerRegistrationResponse response = new WebHookListenerRegistrationResponse(
                webHookListenerParameters,  URI.create("http://imperial-jira.gov.empire/WEBHOOK-1"), "Rebel spy", date.getTime());

        RegistrationParametersAdapter.AdaptedRegistrationParameters marshaledResponse = parametersAdapter.marshal(response);
        assertThat(marshaledResponse, Matchers.instanceOf(RegistrationParametersAdapter.AdaptedRegistrationResponse.class));
        RegistrationParametersAdapter.AdaptedRegistrationResponse registrationResponse = (RegistrationParametersAdapter.AdaptedRegistrationResponse) marshaledResponse;

        assertEquals("REBEL WEBHOOK", registrationResponse.name);
        assertEquals(URI.create("http://imperial-jira.gov.empire/WEBHOOK-1"), registrationResponse.self);
        assertEquals("Rebel spy", registrationResponse.lastUpdatedUser);

        assertThat(response.getEvents(), new ContainsEventMatcher(Sets.newHashSet("jira:issue_created", "jira:issue_updated")));
    }

    private static final class ContainsEventMatcher extends TypeSafeMatcher<Iterable<String>>
    {
        private final Set<String> expectedEvents;

        private ContainsEventMatcher(Set<String> expectedEvents)
        {
            this.expectedEvents = expectedEvents;
        }

        @Override
        public boolean matchesSafely(Iterable<String> events)
        {
            return Iterables.all(Sets.newHashSet(events), new Predicate<String>()
            {
                @Override
                public boolean apply(final String event)
                {
                    return expectedEvents.contains(event);
                }
            });
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Expected the following set of expectedEvents " + expectedEvents);
        }
    }

}
