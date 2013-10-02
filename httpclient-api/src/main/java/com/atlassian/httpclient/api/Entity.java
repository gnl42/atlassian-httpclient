package com.atlassian.httpclient.api;

import java.io.InputStream;

public interface Entity
{
    Headers headers();

    InputStream inputStream();

    String asString();

    interface Builder extends Buildable<Entity>
    {
    }
}
