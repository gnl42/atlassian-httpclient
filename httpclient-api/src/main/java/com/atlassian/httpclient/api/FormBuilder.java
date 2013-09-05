package com.atlassian.httpclient.api;

/**
 * Builds url-encoded form entities for use as HTTP request message bodies. URL encoding of parameter names and values
 * is handled by FormBuilder implementations.
 */
public interface FormBuilder extends Entity.Builder
{
    /**
     * Adds a value-less parameter.
     *
     * @param name The name of the parameter
     */
    public FormBuilder addParam(String name);

    /**
     * Adds a parameter and its value.
     *
     * @param name The name of the parameter
     * @param value The value of the parameter
     */
    public FormBuilder addParam(String name, String value);

    /**
     * Sets multiple values for the named parameter, resetting any existing values in the process.
     *
     * @param name The name of the parameter
     * @param values all values for the named the parameter
     */
    public FormBuilder setParam(String name, Iterable<String> values);
}
