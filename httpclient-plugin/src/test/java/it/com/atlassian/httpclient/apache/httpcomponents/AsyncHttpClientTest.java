package it.com.atlassian.httpclient.apache.httpcomponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests that we can start the plugin.
 */
public class AsyncHttpClientTest
{
    @Test
    public void testPluginStarts() throws IOException
    {
        String baseurl = System.getProperty("baseurl");
        if (!baseurl.endsWith("/"))
        {
            baseurl = baseurl + "/";
        }

        URI baseUri = URI.create(baseurl);
        HttpHost httpHost = new HttpHost(baseUri.getHost(), baseUri.getPort(), baseUri.getScheme());
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin:admin");
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        AuthCache authCache = new BasicAuthCache();
        authCache.put(httpHost, new BasicScheme());

        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        context.setCredentialsProvider(credentialsProvider);

        HttpClient client = HttpClientBuilder.create()
                .build();
        URI pluginStateUri = baseUri.resolve("rest/plugins/1.0/com.atlassian.httpclient.atlassian-httpclient-plugin-key");
        HttpGet request = new HttpGet(pluginStateUri);
        HttpResponse response = client.execute(request, context);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getEntity().getContent());
        assertThat("Expected plugin enabled: " + root, root.get("enabled").asBoolean(), is(true));
    }
}
