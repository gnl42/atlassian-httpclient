package it.com.atlassian.webhooks.plugin;


import com.atlassian.functest.rest.TestResults;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.*;

public final class TestWebHookInPlugin
{
    @Test
    public void run() throws IOException, JAXBException
    {
        File targetDir = new File("target");
        final UriBuilder builder = new UriBuilder(Uri.parse(getUrl("/rest/functest/latest/junit/runTests")))
                .putQueryParameter("outdir", targetDir.getAbsolutePath())
                .putQueryParameter("groups", "all");

        InputStream in = new URL(builder.toString()).openStream();
        TestResults results = (TestResults) JAXBContext.newInstance(TestResults.class).createUnmarshaller().unmarshal(in);
        assertNotNull(results);

        System.out.println("Results: " + results.output);

        assertEquals(0, results.result); // make sure that the number of failing tests is 0
    }

    private String getUrl(String path)
    {
        return getBaseUrl() + path;
    }

    private String getBaseUrl()
    {
        return System.getProperty("baseurl", "http://localhost:5990/refapp");
    }
}
