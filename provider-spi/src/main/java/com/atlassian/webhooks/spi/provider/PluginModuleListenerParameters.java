package com.atlassian.webhooks.spi.provider;

import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Parameters of WebHookListener listening for plugin events.
 */
public final class PluginModuleListenerParameters
{
    private final String pluginKey;
    private final Optional<String> moduleKey;
    private final Map<String, Object> params;
    private final String eventIdentifier;

    public PluginModuleListenerParameters(String pluginKey, Optional<String> moduleKey, Map<String, Object> params, final String eventIdentifier)
    {
        this.pluginKey = checkNotNull(pluginKey);
        this.moduleKey = checkNotNull(moduleKey);
        this.params = checkNotNull(params);
        this.eventIdentifier = checkNotNull(eventIdentifier);
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public Optional<String> getModuleKey()
    {
        return moduleKey;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public String getEventIdentifier()
    {
        return eventIdentifier;
    }
}
