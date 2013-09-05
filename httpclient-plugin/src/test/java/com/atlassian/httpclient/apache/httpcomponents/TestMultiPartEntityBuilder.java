package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Headers;
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

public class TestMultiPartEntityBuilder
{

    @Test
    public void testBuildMultipartEntity() throws IOException, URISyntaxException
    {
        final File file = new File(getClass().getResource("/com/atlassian/httpclient/apache/httpcomponents/multipart-test-file").toURI());
        final MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntity.addPart("file", new FileBody(file, "application/octet-stream"));

        final MultipartEntityBuilder builder = MultipartEntityBuilder.builder().entity(multipartEntity);
        final Entity entity = builder.build();

        final Headers headers = entity.headers();
        Option<String> contentType = headers.get("Content-Type");
        Assert.assertTrue(contentType.isDefined());
        Assert.assertThat(contentType.get(), new StringStartsWith("multipart/form-data"));
        Assert.assertThat(contentType.get(), new StringContains("boundary="));

        final String multiPartContent = CharStreams.toString(new InputStreamReader(entity.inputStream()));
        Assert.assertThat(multiPartContent, new StringContains("This is a file which tests multipart entity builder."));
        Assert.assertThat(multiPartContent, new StringContains("application/octet-stream"));
        Assert.assertThat(multiPartContent, new StringContains("Content-Disposition: form-data; name=\"file\"; filename=\"multipart-test-file\""));
    }


}
