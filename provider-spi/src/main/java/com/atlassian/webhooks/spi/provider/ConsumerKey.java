package com.atlassian.webhooks.spi.provider;

import com.google.common.base.Objects;

/**
 * Provides methods necessary to match a consumer with a webhook event.
 */
public class ConsumerKey
{

    private final String pluginKey;
    private final String moduleKey;

    public ConsumerKey(final String pluginKey, final String moduleKey)
    {
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getModuleKey()
    {
        return moduleKey;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pluginKey, moduleKey);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ConsumerKey other = (ConsumerKey) obj;

        return Objects.equal(this.pluginKey, other.pluginKey)
                && Objects.equal(this.moduleKey, other.moduleKey);
    }
}
