package com.atlassian.webhooks.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.PluginUriCustomizer;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PublishTaskFactoryImpl implements PublishTaskFactory
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpClient httpClient;
    private final RequestSigner requestSigner;
    private final PluginUriResolver pluginUriResolver;
    private final UserManager userManager;
    private final PluginUriCustomizer pluginUriCustomizer;

    public PublishTaskFactoryImpl(HttpClient httpClient, RequestSigner requestSigner, PluginUriResolver pluginUriResolver, UserManager userManager, PluginUriCustomizer pluginUriCustomizer)
    {
        this.httpClient = httpClient;
        this.requestSigner = requestSigner;
        this.pluginUriResolver = pluginUriResolver;
        this.userManager = userManager;
        this.pluginUriCustomizer = pluginUriCustomizer;
    }

    @Override
    public PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookConsumer consumer)
    {
        return new PublishTaskImpl(
                httpClient,
                requestSigner,
                consumer,
                getConsumerUri(webHookEvent, consumer),
                getUserName(),
                consumer.getConsumableBodyJson(webHookEvent.getJson())
        );
    }

    private URI getConsumerUri(WebHookEvent webHookEvent, WebHookConsumer consumer)
    {
        Optional<URI> uri = pluginUriResolver.getUri(consumer.getPluginKey(), consumer.getPath());
        if (uri.isPresent())
        {
            return pluginUriCustomizer.customizeURI(consumer.getPluginKey(), uri.get(), webHookEvent);
        }
        else
        {
            logger.error("Could not resolve uri for event '{}' and consumer '{}'", webHookEvent, consumer);
            throw new RuntimeException("Could not resolve uri for event " + webHookEvent + " and consumer " + consumer);
        }
    }

    private String getUserName()
    {
        return Strings.nullToEmpty(userManager.getRemoteUsername());
    }

    static final class PublishTaskImpl implements PublishTask
    {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final HttpClient httpClient;
        private final RequestSigner requestSigner;
        private final WebHookConsumer consumer;
        private final URI uri;
        private final String userName;
        private final String body;

        PublishTaskImpl(HttpClient httpClient,
                        RequestSigner requestSigner,
                        WebHookConsumer consumer,
                        URI uri,
                        String userName,
                        String body)
        {

            this.httpClient = checkNotNull(httpClient);
            this.requestSigner = checkNotNull(requestSigner);
            this.consumer = checkNotNull(consumer);
            this.uri = checkNotNull(uri);
            this.userName = checkNotNull(userName);
            this.body = checkNotNull(body);
        }

        @Override
        public void run()
        {
            final URI uri = getUri();
            if (logger.isDebugEnabled())
            {
                logger.debug("Posting to web hook at '{}', body is:\n{}\n", uri, body);
            }

            // our job is just to send this, not worry about whether it failed or not
            final Request request = httpClient.newRequest(uri, "application/json", body)
                    // attributes capture optional properties sent to analytics
                    .setAttribute("purpose", "web-hook-notification")
                    .setAttribute("pluginKey", consumer.getPluginKey());

            requestSigner.sign(consumer.getPluginKey(), request);
            request.post().transform().clientError(new Function<Response, Object>() {
                @Override
                public Object apply(Response response) {
                    logger.error("Client error - {} when posting to web hook at '{}', body is:\n{}", new Object[] {response.getStatusCode(), uri, body});
                    return null;
                }
            }).serverError(new Function<Response, Object>() {
                @Override
                public Object apply(Response response) {
                    logger.error("Server error - {} when posting to web hook at '{}', body is:\n{}", new Object[]{response.getStatusCode(), uri, body});
                    return null;
                }
            }).toPromise();
        }

        URI getUri()
        {
            Uri parsedUri = Uri.fromJavaUri(uri);
            return new UriBuilder().
                    setScheme(parsedUri.getScheme()).
                    setAuthority(uri.getAuthority()).
                    setPath(uri.getPath()).
                    setQuery(parsedUri.getQuery()).
                    addQueryParameter("user_id", userName).
                    toUri().
                    toJavaUri();
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(PublishTask.class)
                    .add("consumerKey", consumer)
                    .add("userName", userName)
                    .add("uri", uri)
                    .add("body", body)
                    .toString();
        }
    }
}
