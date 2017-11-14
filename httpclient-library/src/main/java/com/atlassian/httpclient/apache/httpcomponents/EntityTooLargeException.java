package com.atlassian.httpclient.apache.httpcomponents;

import org.apache.http.HttpResponse;

import java.io.IOException;

public class EntityTooLargeException extends IOException {

    private final HttpResponse response;

    public EntityTooLargeException(HttpResponse response, String message) {
        super(message);

        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
