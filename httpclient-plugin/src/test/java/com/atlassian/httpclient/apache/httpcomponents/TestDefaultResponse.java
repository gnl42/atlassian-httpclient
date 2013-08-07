package com.atlassian.httpclient.apache.httpcomponents;

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
            DefaultResponse response = new DefaultResponse(100);
            response.setHeader("Content-Length", "50");
            response.setEntityStream(new GeneratingInputStream('x', 50L));
            String data = response.getEntity();
            assertEquals(StringUtils.repeat("x", 50), data);
        }

        @Test
        public void testRetrievalWithinLimitsNoLength() throws ExecutionException, InterruptedException, IOException
        {
            DefaultResponse response = new DefaultResponse(100);
            response.setEntityStream(new GeneratingInputStream('x', 50L));
            String data = response.getEntity();
            assertEquals(StringUtils.repeat("x", 50), data);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testRetrievalOutsideLimitsWithLength() throws ExecutionException, InterruptedException, IOException
        {
            DefaultResponse response = new DefaultResponse(100);
            response.setHeader("Content-Length", "110");
            response.setEntityStream(new GeneratingInputStream('x', 110L));
            response.getEntity();
        }

        @Test(expected = IllegalArgumentException.class)
        public void testRetrievalOutsideLimitsNoLength() throws ExecutionException, InterruptedException, IOException
        {
            DefaultResponse response = new DefaultResponse(100);
            response.setEntityStream(new GeneratingInputStream('x', 150L));
            response.getEntity();
        }
}
