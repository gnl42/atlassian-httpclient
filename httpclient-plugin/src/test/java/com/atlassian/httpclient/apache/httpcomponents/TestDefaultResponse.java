package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Response;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestDefaultResponse
{
    @Test
    public void testRetrievalWithinLimits() throws ExecutionException, InterruptedException, IOException
    {
        Response response = DefaultResponse.builder()
                .setMaxEntitySize(100)
                .setHeader("Content-Length", "50")
                .setEntityStream(new GeneratingInputStream('x', 50L))
                .build();
        String data = response.getEntity();
        assertEquals(StringUtils.repeat("x", 50), data);
    }

    @Test
    public void testRetrievalWithinLimitsNoLength() throws ExecutionException, InterruptedException, IOException
    {
        Response response = DefaultResponse.builder()
                .setMaxEntitySize(100)
                .setEntityStream(new GeneratingInputStream('x', 50L))
                .build();
        String data = response.getEntity();
        assertEquals(StringUtils.repeat("x", 50), data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetrievalOutsideLimitsWithLength() throws ExecutionException, InterruptedException, IOException
    {
        Response response = DefaultResponse.builder()
                .setMaxEntitySize(100)
                .setHeader("Content-Length", "110")
                .setEntityStream(new GeneratingInputStream('x', 110L))
                .build();
        response.getEntity();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetrievalOutsideLimitsNoLength() throws ExecutionException, InterruptedException, IOException
    {
        Response response = DefaultResponse.builder()
                .setMaxEntitySize(100)
                .setEntityStream(new GeneratingInputStream('x', 150L))
                .build();
        response.getEntity();
    }
}
