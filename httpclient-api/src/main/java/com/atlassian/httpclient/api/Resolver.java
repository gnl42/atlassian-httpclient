package com.atlassian.httpclient.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface Resolver {
    InetAddress[] resolve(String var1) throws UnknownHostException;
}

