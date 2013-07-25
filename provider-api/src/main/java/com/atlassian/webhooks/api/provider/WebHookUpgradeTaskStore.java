package com.atlassian.webhooks.api.provider;


import java.util.Date;

/**
 * Store provided for upgrade task which migrates data from jira-webhooks-plugin to atlassian-webhooks-plugin.
 */
public interface WebHookUpgradeTaskStore
{

    boolean saveWebHook(
            final Integer id,
            final String name,
            final String targetUrl,
            final Iterable<String> events,
            final String params,
            final boolean enabled,
            final String username,
            final Date lastUpdatedDate,
            final WebHookListenerService.RegistrationMethod registrationMethod);
}
