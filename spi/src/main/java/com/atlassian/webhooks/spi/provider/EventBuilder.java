package com.atlassian.webhooks.spi.provider;

public interface EventBuilder
{
    <E> MapperBuilder<E> whenFired(Class<E> eventClass);
}
