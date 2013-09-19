package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.FormBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static junit.framework.Assert.assertEquals;

public class DefaultFormBuilderTest
{
    @Test
    public void testOneEmptyParam()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        assertEquals("foo", toString(form));
    }

    @Test
    public void testTwoLikeEmptyParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        form.addParam("foo");
        assertEquals("foo&foo", toString(form));
    }

    @Test
    public void testTwoEmptyParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        form.addParam("bar");
        assertEquals("foo&bar", toString(form));
    }

    @Test
    public void testOneParam()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo", "bar");
        assertEquals("foo=bar", toString(form));
    }

    @Test
    public void testTwoLikeParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one", "a");
        form.addParam("one", "b");
        assertEquals("one=a&one=b", toString(form));
    }

    @Test
    public void testTwoParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one", "1");
        form.addParam("two", "2");
        assertEquals("one=1&two=2", toString(form));
    }

    @Test
    public void testUrlEncoding()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one param", "one value");
        form.addParam("two/param", "two/value");
        form.addParam("three∫param", "three∫value");
        form.addParam("four&param", "four&value");
        assertEquals("one+param=one+value&two%2Fparam=two%2Fvalue&three%E2%88%ABparam=three%E2%88%ABvalue&four%26param=four%26value", toString(form));
    }

    @Test
    public void testHeaders()
    {
        FormBuilder form = new DefaultFormBuilder();
        Entity entity = form.build();
        assertEquals("application/x-www-form-urlencoded", entity.headers().get("Content-Type").get());
        assertEquals(Charsets.UTF_8, entity.headers().contentCharset().get());
    }

    private static String toString(final FormBuilder form)
    {
        try
        {
            return CharStreams.toString(CharStreams.newReaderSupplier(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return form.build().inputStream();
                }
            }, Charset.forName("UTF-8")));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
