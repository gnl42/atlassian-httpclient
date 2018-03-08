package com.atlassian.httpclient.api;

import java.net.UnknownHostException;

/**
 * This is to be thrown when a host has been blacklisted by IP and a connection should not be attempted.
 *
 * It extends {@link UnknownHostException} due to the semantics around where it must be thrown. It is utilised in the
 * {@link HostResolver} which can only throw an UnknownHostException.
 */
public class BannedHostException extends UnknownHostException {

    public BannedHostException(String desc) {
        super(desc);
    }
}
