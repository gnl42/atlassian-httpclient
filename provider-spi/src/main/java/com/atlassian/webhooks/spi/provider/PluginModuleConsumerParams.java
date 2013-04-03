package com.atlassian.webhooks.spi.provider;

import com.google.common.base.Optional;

import java.util.Map;

public final class PluginModuleConsumerParams
{
    private final String pluginKey;
    private final Optional<String> moduleKey;
    private final Map<String, Object> params;

    public PluginModuleConsumerParams(String pluginKey, Optional<String> moduleKey, Map<String, Object> params)
    {
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.params = params;
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
}
