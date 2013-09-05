package com.atlassian.httpclient.api;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;

/**
 * Attributes are request metadata that are forwarded to the analytics plugin
 * when enabled.
 */
public interface Attributes extends Iterable<Pair<String, String>>
{
    Option<String> get(String name);
}
