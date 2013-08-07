package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.EntityBuilder;
import com.google.common.io.CharStreams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Map;

public class TestMultiPartEntityBuilder
{

    @Test
    public void testBuildMultipartEntity() throws IOException, URISyntaxException
    {
        final File file = new File(getClass().getResource("/com/atlassian/httpclient/apache/httpcomponents/multipart-test-file").toURI());
        final MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntity.addPart("file", new FileBody(file, "application/octet-stream"));

        final MultiPartEntityBuilder builder = new MultiPartEntityBuilder(multipartEntity);
        final EntityBuilder.Entity entity = builder.build();

        final Map<String, String> headers = entity.getHeaders();
        Assert.assertTrue(headers.containsKey("Content-Type"));
        Assert.assertThat(headers.get("Content-Type"), new StringStartsWith("multipart/form-data"));
        Assert.assertThat(headers.get("Content-Type"), new StringContains("boundary="));

        final String multiPartContent = CharStreams.toString(new InputStreamReader(entity.getInputStream()));
        Assert.assertThat(multiPartContent, new StringContains("This is a file which tests multipart entity builder."));
        Assert.assertThat(multiPartContent, new StringContains("application/octet-stream"));
        Assert.assertThat(multiPartContent, new StringContains("Content-Disposition: form-data; name=\"file\"; filename=\"multipart-test-file\""));
    }


}
