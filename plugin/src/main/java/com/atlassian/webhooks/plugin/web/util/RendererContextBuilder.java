package com.atlassian.webhooks.plugin.web.util;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class RendererContextBuilder
{
    private final Map<String, Object> context;

    public RendererContextBuilder()
    {
        context = new HashMap<String, Object>();
    }

    public RendererContextBuilder(Map<String, Object> context)
    {
        this();
        for (Map.Entry<String, Object> entry : context.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    public RendererContextBuilder put(String name, Object value)
    {
        if (value != null)
        {
            context.put(name, value);
        }
        return this;
    }

    public Map<String, Object> build()
    {
        return ImmutableMap.copyOf(context);
    }
}
