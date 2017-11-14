package com.atlassian.httpclient.apache.httpcomponents;

import com.google.common.primitives.Ints;
import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;

import java.io.IOException;

/**
 * An AsyncResponseConsumer that buffers input until the buffer contains {@code maxEntitySize} bytes. If more data
 * is read, a {@link ContentTooLongException} is thrown.
 *
 * @since 0.23.5
 */
public class BoundedAsyncResponseConsumer extends AbstractAsyncResponseConsumer<HttpResponse> {

    // limit the amount of memory that is pre-allocated based on the reported Content-Length. Let's be a bit paranoid
    private static final int MAX_INITIAL_BUFFER_SIZE = 256 * 1024;

    private final int maxEntitySize;

    private volatile BoundedInputBuffer buf;
    private volatile HttpResponse response;

    BoundedAsyncResponseConsumer(int maxEntitySize) {
        this.maxEntitySize = maxEntitySize;
    }

    protected HttpResponse buildResult(HttpContext context) {
        return response;
    }

    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        Asserts.notNull(buf, "Content buffer");
        try {
            buf.consumeContent(decoder);
        } catch (BufferFullException e) {
            throw new EntityTooLargeException(response,
                    "Entity content is too long; larger than " + maxEntitySize + " bytes");
        }
    }

    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
        int length = Math.min(Ints.saturatedCast(entity.getContentLength()), maxEntitySize);
        if (length < 0L) {
            // start with a 4k buffer
            length = Math.min(4096, maxEntitySize);
        }
        int initialBufferSize = Math.min(MAX_INITIAL_BUFFER_SIZE, length);

        buf = new BoundedInputBuffer(initialBufferSize, maxEntitySize, new HeapByteBufferAllocator());
        Asserts.notNull(response, "response");
        response.setEntity(new ContentBufferEntity(entity, buf));
    }

    protected void onResponseReceived(HttpResponse response) throws IOException {
        this.response = response;
    }

    protected void releaseResources() {
        this.response = null;
        this.buf = null;
    }

    private static class BoundedInputBuffer extends SimpleInputBuffer {

        private final int maxSize;

        BoundedInputBuffer(int initialSize, int maxSize, ByteBufferAllocator allocator) {
            super(Math.min(maxSize, initialSize), allocator);

            this.maxSize = maxSize;
        }

        @Override
        protected void expand() {
            int capacity = buffer.capacity();
            int newCapacity = capacity < 2 ? 2 : capacity + (capacity >>> 1);
            if (newCapacity < capacity) {
                // must be integer overflow
                newCapacity = Integer.MAX_VALUE;
            }
            ensureCapacity(newCapacity);
        }

        @Override
        protected void ensureCapacity(int requiredCapacity) {
            if (buffer.capacity() == maxSize && requiredCapacity > maxSize) {
                throw new BufferFullException();
            }
            super.ensureCapacity(Math.min(requiredCapacity, maxSize));
        }
    }

    private static class BufferFullException extends RuntimeException {
    }
}
