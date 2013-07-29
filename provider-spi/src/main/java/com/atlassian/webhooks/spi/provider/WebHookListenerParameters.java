package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

import java.util.Date;
import java.util.Map;

@PublicSpi
public interface WebHookListenerParameters extends WebHookListenerRegistrationParameters
{
    /**
     * Returns id of the registered WebHook Listener.
     */
    Integer getId();

    /**
     * Returns true if the WebHook Listener is enabled, otherwise returns
     */
    Boolean isEnabled();

    Date getLastUpdated();

    String getLastUpdatedUser();

    String getRegistrationMethod();

    class WebHookListenerParametersImpl implements WebHookListenerParameters
    {
        private final int id;
        private final Boolean enabled;
        private final Date lastUpdated;
        private final String lastUpdatedUser;
        private final String name;
        private final String url;
        private final Map<String, Object> parameters;
        private final Iterable<String> events;
        private final String registrationMethod;

        public WebHookListenerParametersImpl(int id, Boolean enabled, Date lastUpdated, String lastUpdatedUser, String name,
                String url, Map<String, Object> parameters, Iterable<String> events, String registrationMethod)
        {
            this.id = id;
            this.enabled = enabled;
            this.lastUpdated = lastUpdated;
            this.lastUpdatedUser = lastUpdatedUser;
            this.name = name;
            this.url = url;
            this.parameters = parameters;
            this.events = events;
            this.registrationMethod = registrationMethod;
        }

        @Override
        public Integer getId()
        {
            return id;
        }

        @Override
        public Boolean isEnabled()
        {
            return enabled;
        }

        @Override
        public Date getLastUpdated()
        {
            return lastUpdated;
        }

        @Override
        public String getLastUpdatedUser()
        {
            return lastUpdatedUser;
        }

        @Override
        public String getRegistrationMethod()
        {
            return registrationMethod;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getUrl()
        {
            return url;
        }

        @Override
        public Map<String, Object> getParameters()
        {
            return parameters;
        }

        @Override
        public Iterable<String> getEvents()
        {
            return events;
        }
    }

}
