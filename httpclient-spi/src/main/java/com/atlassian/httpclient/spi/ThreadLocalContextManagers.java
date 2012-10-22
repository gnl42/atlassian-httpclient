package com.atlassian.httpclient.spi;

public final class ThreadLocalContextManagers
{
    private ThreadLocalContextManagers()
    {
    }

    public static <C> ThreadLocalContextManager<C> noop()
    {
        return new ThreadLocalContextManager<C>()
        {
            @Override
            public C getThreadLocalContext()
            {
                return null;
            }

            @Override
            public void setThreadLocalContext(Object context)
            {
            }

            @Override
            public void resetThreadLocalContext()
            {
            }
        };
    }
}
