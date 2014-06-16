package com.atlassian.httpclient.api.factory;

/**
 * Represents a scheme for communicating with a host (e.g. HTTP or HTTPS)
 *
 */
public enum Scheme
{
    HTTP("http"), HTTPS("https");

    private final String schemeName;

    public String schemeName()
    {
        return schemeName;
    }

    private Scheme(String name)
    {
        schemeName = name;
    }
}
