package com.atlassian.httpclient.spi;

public interface ThreadLocalContextManager<C>
{
    C getThreadLocalContext();

    void setThreadLocalContext(C context);

    void resetThreadLocalContext();
}
