package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.service.WebHookListenerEventJoiner;
import com.google.common.collect.Iterables;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class WebHookListenerRegistrationResponse
{
    @XmlElement
    private final String name;

    @XmlElement
    private final String url;

    @XmlElement
    private final URI self;

    @XmlElement
    private final String parameters;

    @XmlElement
    private final String lastUpdatedUser;

    @XmlElement
    private final String lastUpdatedDisplayName;

    @XmlElement
    private final Long lastUpdated;

    @XmlElement
    private final boolean enabled;

    @XmlElement
    private final String[] events;

    public WebHookListenerRegistrationResponse(final WebHookAO webHookAO, URI self, String lastUpdatedDisplayName, Long lastUpdated)
    {
        this.self = self;
        this.name = webHookAO.getName();
        this.url = webHookAO.getUrl();
        this.lastUpdatedDisplayName = lastUpdatedDisplayName;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedUser = webHookAO.getLastUpdatedUser();
        this.enabled = webHookAO.isEnabled();
        this.parameters = webHookAO.getParameters();
        this.events = Iterables.toArray(WebHookListenerEventJoiner.split(webHookAO.getEvents()), String.class);
    }

    public static class Factory
    {
        private final UserManager userManager;

        public Factory(UserManager userManager)
        {
            this.userManager = userManager;
        }

        public WebHookListenerRegistrationResponse create(WebHookAO webHookAO, URI self)
        {
            final UserProfile userProfile = userManager.getUserProfile(webHookAO.getLastUpdatedUser());
            final String userFullName = userProfile != null ? userProfile.getFullName() : webHookAO.getLastUpdatedUser();
            // TODO we just got rid of date formatting
//            final String lastUpdatedShort = formatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.DATE_TIME_PICKER).format(webHookAO.getLastUpdated());

            return new WebHookListenerRegistrationResponse(webHookAO, self, userFullName, webHookAO.getLastUpdated().getTime());
        }

    }
}
