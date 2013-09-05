package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.util.concurrent.Promise;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

interface AsyncHttpClient
{
    Promise<HttpResponse> execute(HttpUriRequest request, HttpContext context);
}
