package com.atlassian.webhooks.spi.provider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class EventSerializers
{
    private EventSerializers()
    {
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
}
