package com.atlassian.webhooks.spi.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface WebHook
{
    String id();

    Class<? extends EventMatcher> matcher() default EventMatcher.EventClassEventMatcher.class;

    Class<? extends EventSerializerFactory> serializerFactory() default ReflectionEventSerializerFactory.class;

    static final class ReflectionEventSerializerFactory implements EventSerializerFactory
    {
        @Override
        public EventSerializer create(Object event)
        {
            return new EventSerializers.ReflectionEventSerializer(event);
        }
    }
}
