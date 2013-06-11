package com.atlassian.webhooks.spi.provider;

public interface WebHookRegistrationParameters
{
    String getEvents();

    String getUrl();

    String getParameters();
}
