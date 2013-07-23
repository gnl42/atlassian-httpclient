package com.atlassian.webhooks.plugin.rest;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;

import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(RegistrationParametersAdapter.class)
public class WebHookListenerRegistrationResponse extends WebHookListenerRegistration
{
    private final URI self;
    private final String lastUpdatedUser;
    private final String lastUpdatedDisplayName;
    private final Long lastUpdated;

    public WebHookListenerRegistrationResponse(WebHookListenerParameters listenerParameters, URI self, String lastUpdatedDisplayName, Long lastUpdated)
    {
        super(listenerParameters.getName(), listenerParameters.getUrl(), listenerParameters.getParameters(), listenerParameters.getEvents(), listenerParameters.isEnabled());
        this.lastUpdatedDisplayName = lastUpdatedDisplayName;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedUser = listenerParameters.getLastUpdatedUser();
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

    public static class Factory
    {
        private final UserManager userManager;

        public Factory(UserManager userManager)
        {
            this.userManager = userManager;
        }

        public WebHookListenerRegistrationResponse create(WebHookListenerParameters listenerParameters, URI self)
        {
            final UserProfile userProfile = userManager.getUserProfile(listenerParameters.getLastUpdatedUser());
            final String userFullName = userProfile != null ? userProfile.getFullName() : listenerParameters.getLastUpdatedUser();
            return new WebHookListenerRegistrationResponse(listenerParameters, self, userFullName, listenerParameters.getLastUpdated().getTime());
        }
    }
}
