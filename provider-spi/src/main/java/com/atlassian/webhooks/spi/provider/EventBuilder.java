package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface EventBuilder
{
    <E> MapperBuilder<E> whenFired(Class<E> eventClass);
}
