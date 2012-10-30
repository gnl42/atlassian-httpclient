package com.atlassian.webhooks.plugin;

public interface ConstructionStrategy
{
    <T> T get(Class<T> type);
}
