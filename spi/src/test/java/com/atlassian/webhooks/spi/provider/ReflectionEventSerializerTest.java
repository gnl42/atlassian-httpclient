package com.atlassian.webhooks.spi.provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public final class ReflectionEventSerializerTest
{
    @Test
    public void testToMapForObject()
    {
        final Map<String, Object> map = EventSerializers.ReflectionEventSerializer.toMap(new Object());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToMap()
    {
        final int _int = 123;
        final int _myClass_int = 1234;
        int _iterable_0 = 12345;
        int _iterable_1_int = 123456;

        final Map<String, Object> map = EventSerializers.ReflectionEventSerializer.toMap(new MyClass(_int, ImmutableList.of(_iterable_0, new MyClass(_iterable_1_int, null, null)), new MyClass(_myClass_int, null, null)));

        assertEquals(_int, map.get("_int"));
        assertEquals(_myClass_int, ((Map<String, Object>) map.get("_myClass")).get("_int"));
        assertEquals(_iterable_0, Iterables.get((Iterable<Object>) map.get("_iterable"), 0));
        assertEquals(_iterable_1_int, ((Map<String, Object>) Iterables.get((Iterable<Object>) map.get("_iterable"), 1)).get("_int"));
    }

    private final static class MyClass
    {
        private final int _int;
        private final MyClass _myClass;
        private final Iterable<Object> _iterable;

        private MyClass(int anInt, Iterable<Object> iterable, MyClass myClass)
        {
            _int = anInt;
            _iterable = iterable;
            _myClass = myClass;
        }
    }
}
