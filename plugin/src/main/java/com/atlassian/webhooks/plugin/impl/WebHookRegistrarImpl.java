package com.atlassian.webhooks.plugin.impl;

import com.atlassian.webhooks.plugin.WebHookRegistration;
import com.atlassian.webhooks.spi.provider.EventBuilder;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public final class WebHookRegistrarImpl implements WebHookRegistrar
{
    private final Set<WebHookRegistration> registrations = newHashSet();

    @Override
    public EventBuilder webhook(String id)
    {
        WebHookRegistration registration = new WebHookRegistration(id);
        registrations.add(registration);
        return new EventBuilderImpl(registration);
    }

    public Set<WebHookRegistration> getRegistrations()
    {
        return ImmutableSet.copyOf(registrations);
    }
}
