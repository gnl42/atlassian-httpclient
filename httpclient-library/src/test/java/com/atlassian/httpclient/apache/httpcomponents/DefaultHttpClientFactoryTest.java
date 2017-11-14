package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DefaultHttpClientFactoryTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ThreadLocalContextManager<?> threadLocalContextManager;

    @Test
    public void testDisposingHttpClient() throws Exception {
        DefaultHttpClientFactory<?> factory = new DefaultHttpClientFactory<>(eventPublisher, applicationProperties, threadLocalContextManager);
        final HttpClient httpClient1 = factory.create(new HttpClientOptions());
        final HttpClient httpClient2 = factory.create(new HttpClientOptions());

        assertThat(httpClient1, notNullValue());
        assertThat(httpClient2, notNullValue());
        assertThat(factory.getHttpClients(), Matchers.<ApacheAsyncHttpClient>iterableWithSize(2));

        factory.dispose(httpClient1);

        assertThat(factory.getHttpClients(), Matchers.<ApacheAsyncHttpClient>iterableWithSize(1));

        factory.dispose(httpClient2);

        assertThat(factory.getHttpClients(), Matchers.<ApacheAsyncHttpClient>iterableWithSize(0));
    }

    @Test
    public void testDisposingClientTwice() throws Exception {
        DefaultHttpClientFactory<?> factory = new DefaultHttpClientFactory<>(eventPublisher, applicationProperties, threadLocalContextManager);
        final HttpClient httpClient = factory.create(new HttpClientOptions());

        assertThat(httpClient, notNullValue());
        assertThat(factory.getHttpClients(), Matchers.<ApacheAsyncHttpClient>iterableWithSize(1));

        factory.dispose(httpClient);

        assertThat(factory.getHttpClients(), Matchers.<ApacheAsyncHttpClient>iterableWithSize(0));

        exception.expect(IllegalStateException.class);
        exception.expectMessage("Client is already disposed");
        factory.dispose(httpClient);
    }

    @Test
    public void testNotDisposingNotDisposableClient() throws Exception {
        DefaultHttpClientFactory<?> factory = new DefaultHttpClientFactory<>(eventPublisher, applicationProperties, threadLocalContextManager);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Given client is not disposable");
        factory.dispose(Mockito.mock(HttpClient.class));
    }
}
