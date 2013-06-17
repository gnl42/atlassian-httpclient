package com.atlassian.webhooks.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.atlassian.webhooks.spi.provider.WebHookListener;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;

public class PublishTaskFactoryImplTest {

    @Test
    public void testUriParametersParse() {
        PublishTaskFactoryImpl.PublishTaskImpl publishTask = createDefaultPublishTask(
                "http://poszter.herokuapp.com/?thisShouldStayInUrl",
                "admin");

        URI uri = publishTask.getUri();
        Assert.assertEquals("http://poszter.herokuapp.com/?thisShouldStayInUrl&user_id=admin", uri.toString());
    }

    @Test
    public void testUriParametersAndFragmentParse() {
        PublishTaskFactoryImpl.PublishTaskImpl publishTask = createDefaultPublishTask(
                "http://poszter.herokuapp.com/?thisShouldStayInUrl#ThisShouldGone",
                "admin");

        URI uri = publishTask.getUri();
        Assert.assertEquals("http://poszter.herokuapp.com/?thisShouldStayInUrl&user_id=admin", uri.toString());
    }

    /////////////////////////////////////////////////////////////////////////////

    private PublishTaskFactoryImpl.PublishTaskImpl createDefaultPublishTask(String uri, String userName) {
        String body = "Mock Webhook Event";
        URI uriObj = URI.create(uri);
        return new PublishTaskFactoryImpl.PublishTaskImpl(Mockito.mock(HttpClient.class),
            Mockito.mock(RequestSigner.class),
                Mockito.mock(TokenBucket.class), Mockito.mock(WebHookListener.class),
            uriObj,
            userName,
            body);
    }
}
