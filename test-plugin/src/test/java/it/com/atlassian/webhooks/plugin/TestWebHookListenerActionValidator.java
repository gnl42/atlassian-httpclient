package it.com.atlassian.webhooks.plugin;

import com.google.common.io.CharStreams;
import org.apache.axis.encoding.Base64;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestWebHookListenerActionValidator
{
    private static final String WEBHOOK_REST_URI = "http://localhost:5990/refapp/rest/webhooks/1.0/webhook/";
    private final HttpClient client = new DefaultHttpClient();

    @Test
    public void testAddingWebHook() throws IOException
    {
        HttpResponse response = create("Filip's webhook");
        final String responseText = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        final int statusCode = response.getStatusLine().getStatusCode();

        assertThat(responseText, containsString("We're not adding WebHook from Filip"));
        assertThat(statusCode, is(400));
    }

    @Test
    public void testUpdatingWebHook() throws IOException, JSONException
    {
        final HttpResponse createResponse = create("Jonathan's webhook");
        assertThat(createResponse.getStatusLine().getStatusCode(), is(201));
        final JSONObject jsonObject = new JSONObject(CharStreams.toString(new InputStreamReader(createResponse.getEntity().getContent())));
        String self = jsonObject.getString("self");

        final HttpResponse updateResponse = update("Seb's webhook", self);
        final String responseText = CharStreams.toString(new InputStreamReader(updateResponse.getEntity().getContent()));
        assertThat(updateResponse.getStatusLine().getStatusCode(), is(400));
        assertThat(responseText, containsString("Seb is not allowed to updateWebHookListener WebHooks"));
    }

    @Test
    public void testDeletingWebHook() throws IOException, JSONException
    {
        final HttpResponse webHookToImportantToBeDeleted = create("Jonathon's webhook");
        assertThat(webHookToImportantToBeDeleted.getStatusLine().getStatusCode(), is(201));
        String responseText = CharStreams.toString(new InputStreamReader(webHookToImportantToBeDeleted.getEntity().getContent()));
        final String self = new JSONObject(responseText).getString("self");

        HttpResponse response = delete(self);
        responseText = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        assertThat(response.getStatusLine().getStatusCode(), is(409));
        assertThat(responseText, containsString("Jonathon's webhook are to important"));

        final HttpResponse notThatImportantWebHook = create("Not that important webhook");
        assertThat(notThatImportantWebHook.getStatusLine().getStatusCode(), is(201));

        final String notThatImportantWebHookSelf = new JSONObject(CharStreams.toString(new InputStreamReader(notThatImportantWebHook.getEntity().getContent()))).getString("self");

        HttpResponse updateResponse = update("Another name for this meaningless webhook", notThatImportantWebHookSelf);
        responseText = CharStreams.toString(new InputStreamReader(updateResponse.getEntity().getContent()));
        assertThat(updateResponse.getStatusLine().getStatusCode(), is(200));
        assertThat(responseText, containsString("Another name for this meaningless webhook"));

        HttpResponse deleteResponse = delete(notThatImportantWebHookSelf);
        assertThat(deleteResponse.getStatusLine().getStatusCode(), is(204));
    }

    private HttpResponse create(final String name) throws IOException
    {
        final HttpPost postRequest = new HttpPost(WEBHOOK_REST_URI);
        return setEntityAndExecute(name, postRequest);
    }

    private HttpResponse update(final String name, final String self) throws IOException
    {
        final HttpPut updateRequest = new HttpPut(self);
        return setEntityAndExecute(name, updateRequest);
    }

    private HttpResponse delete(final String self) throws IOException
    {
        final HttpDelete deleteRequest = new HttpDelete(self);
        authorize(deleteRequest);
        return client.execute(deleteRequest);
    }

    private HttpResponse setEntityAndExecute(String name, final HttpEntityEnclosingRequestBase request) throws IOException
    {
        request.setEntity(new StringEntity("{ \"name\": \""+ name + "\", \"url\": \"http://localhost:1000/webhook\", \"events\": [\"jira:issue_updated\"], \"parameters\": \"Project = DEMO\"}"));
        request.setHeader("Content-type", "application/json");
        authorize(request);
        return client.execute(request);
    }

    private void authorize(HttpRequest request)
    {
        request.setHeader("Authorization", "Basic " + Base64.encode("admin:admin".getBytes()));
    }

}
