package com.atlassian.webhooks.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.webhooks.spi.plugin.PluginUriResolver;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.atlassian.webhooks.spi.provider.ConsumerKey;
import com.atlassian.webhooks.spi.provider.WebHookConsumer;
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.*;

public final class PublishTaskFactoryImpl implements PublishTaskFactory
{
    private final HttpClient httpClient;
    private final RequestSigner requestSigner;
    private final PluginUriResolver pluginUriResolver;
    private final UserManager userManager;

    public PublishTaskFactoryImpl(HttpClient httpClient, RequestSigner requestSigner, PluginUriResolver pluginUriResolver, UserManager userManager)
    {
        this.httpClient = httpClient;
        this.requestSigner = requestSigner;
        this.pluginUriResolver = pluginUriResolver;
        this.userManager = userManager;
    }

    @Override
    public PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookConsumer consumer)
    {
        return new PublishTaskImpl(
                httpClient,
                requestSigner,
                consumer.getConsumerKey(),
                getConsumerUri(consumer),
                getUserName(),
                webHookEvent.getJson()
        );
    }

    private URI getConsumerUri(WebHookConsumer consumer)
    {
        return pluginUriResolver.getUri(consumer.getConsumerKey().getPluginKey(), consumer.getPath());
    }

    private String getUserName()
    {
        return Strings.nullToEmpty(userManager.getRemoteUsername());
    }

    private static final class PublishTaskImpl implements PublishTask
    {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final HttpClient httpClient;
        private final RequestSigner requestSigner;
        private final ConsumerKey consumerKey;
        private final URI uri;
        private final String userName;
        private final String body;

        PublishTaskImpl(HttpClient httpClient,
                        RequestSigner requestSigner,
                        ConsumerKey consumerKey,
                        URI uri,
                        String userName,
                        String body)
        {

            this.httpClient = checkNotNull(httpClient);
            this.requestSigner = checkNotNull(requestSigner);
            this.consumerKey = checkNotNull(consumerKey);
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
                    .setAttribute("pluginKey", consumerKey.getPluginKey());

            requestSigner.sign(consumerKey.getPluginKey(), request);
            request.post();
        }

        private URI getUri()
        {
            return new UriBuilder(Uri.fromJavaUri(uri)).addQueryParameter("user_id", userName).toUri().toJavaUri();
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(PublishTask.class)
                    .add("consumerKey", consumerKey)
                    .add("userName", userName)
                    .add("uri", uri)
                    .add("body", body)
                    .toString();
        }
    }
}
