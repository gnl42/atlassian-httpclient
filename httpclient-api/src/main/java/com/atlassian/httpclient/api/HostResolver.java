package com.atlassian.httpclient.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface HostResolver {
    InetAddress[] resolve(String var1) throws UnknownHostException;
}

