package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.service.WebHookListenerEventJoiner;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

@XmlJavaTypeAdapter(RegistrationParametersAdapter.class)
public class WebHookListenerRegistrationResponse extends WebHookListenerRegistration
{
    private final URI self;
    private final String lastUpdatedUser;
    private final String lastUpdatedDisplayName;
    private final Long lastUpdated;
    private final boolean enabled;

    public WebHookListenerRegistrationResponse(final WebHookAO webHookAO, URI self, String lastUpdatedDisplayName, Long lastUpdated)
    {
        super(webHookAO.getName(), webHookAO.getUrl(), webHookAO.getParameters(), WebHookListenerEventJoiner.split(webHookAO.getEvents()));
        this.lastUpdatedDisplayName = lastUpdatedDisplayName;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedUser = webHookAO.getLastUpdatedUser();
        this.enabled = webHookAO.isEnabled();
        this.self = self;
    }

    public URI getSelf()
    {
        return self;
    }

    public String getLastUpdatedUser()
    {
        return lastUpdatedUser;
    }

    public String getLastUpdatedDisplayName()
    {
        return lastUpdatedDisplayName;
    }

    public Long getLastUpdated()
    {
        return lastUpdated;
    }

    public boolean isEnabled()
    {
        return enabled;
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
            // TODO bring the data formatting back
//            final String lastUpdatedShort = formatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.DATE_TIME_PICKER).format(webHookAO.getLastUpdated());

            return new WebHookListenerRegistrationResponse(webHookAO, self, userFullName, webHookAO.getLastUpdated().getTime());
        }
    }
}
