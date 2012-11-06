package com.atlassian.httpclient.base.event;

import java.util.Map;

abstract class AbstractHttpRequestEvent
{
    private final String url;
    private final long elapsed;
    private final Map<String, String> properties;

    private int statusCode;
    private String error;

    public AbstractHttpRequestEvent(String url, int statusCode, long elapsed, Map<String, String> properties)
    {
        this.url = url;
        this.statusCode = statusCode;
        this.elapsed = elapsed;
        this.properties = properties;
    }

    public AbstractHttpRequestEvent(String url, String error, long elapsed, Map<String, String> properties)
    {
        this.url = url;
        this.error = error;
        this.elapsed = elapsed;
        this.properties = properties;
    }

    public String getUrl()
    {
        return url;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getError()
    {
        return error;
    }

    public long getElapsed()
    {
        return elapsed;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
