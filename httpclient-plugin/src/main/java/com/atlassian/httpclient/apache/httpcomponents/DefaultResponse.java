package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
import com.atlassian.httpclient.api.Response;

public final class DefaultResponse extends DefaultMessage implements Response
{
    private int statusCode;
    private String statusText;

    DefaultResponse(Option<Entity> entity, Headers headers, int statusCode, String statusText)
    {
        super(headers, entity);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    @Override
    public int statusCode()
    {
        return statusCode;
    }

    @Override
    public String statusText()
    {
        return statusText;
    }

    @Override
    public boolean isInformational()
    {
        return statusCode >= 100 && statusCode < 200;
    }

    @Override
    public boolean isSuccessful()
    {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public boolean isOk()
    {
        return statusCode == 200;
    }

    @Override
    public boolean isCreated()
    {
        return statusCode == 201;
    }

    @Override
    public boolean isNoContent()
    {
        return statusCode == 204;
    }

    @Override
    public boolean isRedirection()
    {
        return statusCode >= 300 && statusCode < 400;
    }

    @Override
    public boolean isSeeOther()
    {
        return statusCode == 303;
    }

    @Override
    public boolean isNotModified()
    {
        return statusCode == 304;
    }

    @Override
    public boolean isClientError()
    {
        return statusCode >= 400 && statusCode < 500;
    }

    @Override
    public boolean isBadRequest()
    {
        return statusCode == 400;
    }

    @Override
    public boolean isUnauthorized()
    {
        return statusCode == 401;
    }

    @Override
    public boolean isForbidden()
    {
        return statusCode == 403;
    }

    @Override
    public boolean isNotFound()
    {
        return statusCode == 404;
    }

    @Override
    public boolean isConflict()
    {
        return statusCode == 409;
    }

    @Override
    public boolean isServerError()
    {
        return statusCode >= 500 && statusCode < 600;
    }

    @Override
    public boolean isInternalServerError()
    {
        return statusCode == 500;
    }

    @Override
    public boolean isServiceUnavailable()
    {
        return statusCode == 503;
    }

    @Override
    public boolean isError()
    {
        return isClientError() || isServerError();
    }

    @Override
    public boolean isNotSuccessful()
    {
        return isInformational() || isRedirection() || isError();
    }
}
