package com.atlassian.httpclient.api.factory;

import javax.annotation.Nonnull;

/**
* Represents a host (host name and port).
*/
public class Host
{
    private final String host;
    private final int port;

    public Host(@Nonnull final String host, final int port)
    {
        if (host == null || host.trim().length() == 0)
            throw new IllegalArgumentException("Host must not be null or empty");
        else if (port <= 0 || port > 65535)
            throw new IllegalArgumentException("Port must be greater than 0 and less than 65535");

        this.host = host;
        this.port = port;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public String toString()
    {
        return "Host{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Host host1 = (Host) o;

        if (port != host1.port)
        {
            return false;
        }
        if (!host.equals(host1.host))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
