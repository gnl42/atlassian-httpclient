package com.atlassian.httpclient.apache.httpcomponents;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BoundedAsyncResponseConsumerTest {

    @Mock
    private HttpContext context;
    @Mock
    private HttpEntity entity;
    @Mock
    private IOControl ioControl;
    @Mock
    private HttpResponse response;

    @Test
    public void testContentReturnedIfActualLengthEqualToMaxLength() throws Exception {
        int maxSize = 4096;
        int actualSize = 4096;
        int chunkSize = 19;

        // stream 1024 bytes of content through the truncating response consumer
        try (InputStream stream = streamContent(maxSize, actualSize, chunkSize)) {

            // verify that exactly 4096 bytes have been buffered
            byte[] buffer = new byte[2 * maxSize];
            assertEquals(actualSize, stream.read(buffer));
            assertEquals(-1, stream.read(buffer));
        }
    }

    @Test
    public void testContentReturnedIfActualLengthLowerThanMaxLength() throws Exception {
        int maxSize = 4096;
        int actualSize = 1024;
        int chunkSize = 21;

        // stream 1024 bytes of content through the truncating response consumer
        try (InputStream stream = streamContent(maxSize, actualSize, chunkSize)) {

            // verify that exactly 1024 bytes have been buffered
            byte[] buffer = new byte[maxSize];
            assertEquals(actualSize, stream.read(buffer));
            assertEquals(-1, stream.read(buffer));
        }
    }

    @Test(expected = EntityTooLargeException.class)
    public void testThrowsIfActualLengthExceedsMaxLength() throws IOException, HttpException {
        int maxSize = 16 * 1024;
        int actualSize = 32 * 1024;
        int chunkSize = 4096;

        // stream 1024 bytes of content through the truncating response consumer
        streamContent(maxSize, actualSize, chunkSize);
    }

    @Test(expected = EntityTooLargeException.class)
    public void testThrowsIfActualLengthExceedsMaxLengthChunked() throws IOException, HttpException {
        int maxSize = 16 * 1024;
        int actualSize = 32 * 1024;
        int chunkSize = 4096;

        // stream 1024 bytes of content through the truncating response consumer
        streamContent(maxSize, actualSize, chunkSize, -1);
    }

    private InputStream streamContent(int maxEntitySize, int actualSize, int chunkSize) throws IOException, HttpException {
        return streamContent(maxEntitySize, actualSize, chunkSize, actualSize);
    }

    private InputStream streamContent(int maxEntitySize, int actualSize, int chunkSize, int contentLength) throws IOException, HttpException {
        doReturn((long) contentLength).when(entity).getContentLength();
        doReturn(entity).when(response).getEntity();

        BoundedAsyncResponseConsumer consumer = new BoundedAsyncResponseConsumer(maxEntitySize);

        consumer.responseReceived(response);

        // buffering entity should now be set on the response
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(response).setEntity(entityCaptor.capture());
        HttpEntity bufferedEntity = entityCaptor.getValue();

        // now create a ContentDecoder that allows reading 8 bytes at a time of a total of 1024 bytes
        StubDecoder decoder = new StubDecoder(actualSize);
        for (int i = 0; i < actualSize; i += chunkSize) {
            assertFalse(decoder.isCompleted());
            decoder.makeAvailable(chunkSize);
            consumer.consumeContent(decoder, ioControl);
        }

        assertTrue(decoder.isCompleted());
        consumer.responseCompleted(context);

        return bufferedEntity.getContent();
    }

    private static class StubDecoder implements ContentDecoder {

        private static final byte CONTENT = 33;
        private long available;
        private long remaining;

        StubDecoder(long length) {
            remaining = length;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int bytesRead = 0;
            while (remaining > 0 && available > 0 && dst.hasRemaining()) {
                dst.put(CONTENT);
                ++bytesRead;
                --available;
                --remaining;
            }

            return remaining == 0 && bytesRead == 0 ? -1 : bytesRead;
        }

        @Override
        public boolean isCompleted() {
            return remaining == 0;
        }

        void makeAvailable(int length) {
            this.available += length;
        }
    }
}