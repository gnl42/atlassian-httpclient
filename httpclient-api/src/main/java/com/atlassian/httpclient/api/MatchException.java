package com.atlassian.httpclient.api;

final class MatchException extends RuntimeException
{
    private static final long serialVersionUID = -9154077277439252556L;

    private final transient Object matched;

    MatchException(Object matched)
    {
        this.matched = matched;
    }

    @Override
    public String getMessage()
    {
        return (matched == null) ? "" : matched.toString();
    }
}
