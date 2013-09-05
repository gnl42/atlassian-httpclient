package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Headers;
import com.atlassian.util.concurrent.Lazy;
import com.atlassian.util.concurrent.Supplier;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

final class LazyBody
{

    static Supplier<String> body(final InputStream instream, final Headers headers, final int maxSize)
    {
        return Lazy.supplier(new Supplier<String>()
        {
            public String get()
            {
                try
                {
                    Charset charset = headers.contentCharset().getOrElse(Charset.defaultCharset());
                    Reader reader = new InputStreamReader(instream, charset);
                    try
                    {
                        int bufferLength = headers.contentLength().getOrElse(4096);

                        CharArrayBuffer buffer = new CharArrayBuffer(bufferLength);
                        char[] tmp = new char[bufferLength];
                        int l;
                        while ((l = reader.read(tmp)) != -1)
                        {
                            if (buffer.length() + l > maxSize)
                            {
                                throw new IllegalStateException("HTTP entity too large to be buffered in memory");
                            }
                            buffer.append(tmp, 0, l);
                        }
                        return buffer.toString();
                    }
                    finally
                    {
                        reader.close();
                        instream.close();
                    }
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("Unable to convert response body to String", e);
                }
            }
        });
    }
}
