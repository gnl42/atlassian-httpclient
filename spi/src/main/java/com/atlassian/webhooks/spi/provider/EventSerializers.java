package com.atlassian.webhooks.spi.provider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

public final class EventSerializers
{
    private EventSerializers()
    {
    }

    public static EventSerializer reflection(Object event)
    {
        return new ReflectionEventSerializer(event);
    }

    public static EventSerializer forMap(Object event, Map<String, Object> data)
    {
        return new MapEventSerializer(event, data);
    }

    private static final class MapEventSerializer implements EventSerializer
    {
        private final Object event;
        private final Map<String, Object> data;

        public MapEventSerializer(Object event, Map<String, Object> data)
        {
            this.event = event;
            this.data = data;
        }

        @Override
        public Object getEvent()
        {
            return event;
        }

        @Override
        public String getJson()
        {
            try
            {
                return new JSONObject(data).toString(2);
            }
            catch (JSONException e)
            {
                throw new EventSerializationException(e);
            }
        }
    }

    static final class ReflectionEventSerializer implements EventSerializer
    {
        private static final Set<Class<?>> RAW_TYPES = ImmutableSet.<Class<?>>builder()
                .add(boolean.class)
                .add(Boolean.class)
                .add(byte.class)
                .add(Byte.class)
                .add(short.class)
                .add(Short.class)
                .add(int.class)
                .add(Integer.class)
                .add(long.class)
                .add(Long.class)
                .add(float.class)
                .add(Float.class)
                .add(double.class)
                .add(Double.class)
                .add(char.class)
                .add(Character.class)
                .add(String.class)
                .build();

        private final Object event;

        ReflectionEventSerializer(Object event)
        {
            this.event = checkNotNull(event);
        }

        @Override
        public Object getEvent()
        {
            return event;
        }

        @Override
        public String getJson() throws EventSerializationException
        {
            return forMap(event, toMap(event)).getJson();
        }

        @VisibleForTesting
        static Map<String, Object> toMap(Object object)
        {
            if (object == null)
            {
                return ImmutableMap.of();
            }

            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            final Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields)
            {
                final Object value = getValue(field, object);
                if (value != null)
                {
                    builder.put(field.getName(), value);
                }
            }
            return builder.build();
        }

        private static Object getValue(Field field, Object object)
        {
            return getTransformedValue(getRawValue(field, object));
        }

        private static Object getTransformedValue(Object object)
        {
            if (object == null)
            {
                return null;
            }
            if (RAW_TYPES.contains(object.getClass()))
            {
                return object;
            }
            if (Iterable.class.isAssignableFrom(object.getClass()))
            {
                return Iterables.transform((Iterable<Object>) object, new Function<Object, Object>()
                {
                    @Override
                    public Object apply(@Nullable Object input)
                    {
                        return getTransformedValue(input);
                    }
                });
            }
            return toMap(object);
        }

        private static Object getRawValue(Field field, Object object)
        {
            final boolean isFieldAccessible = field.isAccessible();
            field.setAccessible(true);
            try
            {
                return field.get(object);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException("Should not happen", e);
            }
            finally
            {
                field.setAccessible(isFieldAccessible);
            }
        }
    }
}
