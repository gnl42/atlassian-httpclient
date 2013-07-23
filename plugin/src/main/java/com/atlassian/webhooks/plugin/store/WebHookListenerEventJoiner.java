package com.atlassian.webhooks.plugin.store;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Joins and splits the vents into a JSON Array.
 */
public class WebHookListenerEventJoiner
{
    public static String join(final Iterable<String> events)
    {
        return new JSONArray(Lists.newArrayList(events)).toString();
    }

    public static Iterable<String> split(final String events)
    {
        try
        {
            final JSONArray eventsJSONArray = new JSONArray(events);
            final List<String> eventsCollection = Lists.newArrayList();
            for (int i = 0; i < eventsJSONArray.length(); i++)
            {
                eventsCollection.add(eventsJSONArray.getString(i));
            }
            return eventsCollection;
        }
        catch (JSONException e)
        {
            throw new IllegalArgumentException("Events " + events + " are not in JSONArray format");
        }
    }

}
