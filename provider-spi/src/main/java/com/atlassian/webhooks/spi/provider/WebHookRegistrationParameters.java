package com.atlassian.webhooks.spi.provider;

public interface WebHookRegistrationParameters
{
    String getName();

    String getEvents();

    String getUrl();

    String getParameters();
}
