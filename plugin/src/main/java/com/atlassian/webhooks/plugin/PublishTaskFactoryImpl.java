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
import com.atlassian.webhooks.spi.provider.WebHookEvent;
import com.atlassian.webhooks.spi.provider.WebHookListener;
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

    private static final int ONE_MINUTE = 60 * 1000;

    private final HttpClient httpClient;
    private final RequestSigner requestSigner;
    private final PluginUriResolver pluginUriResolver;
    private final UserManager userManager;
    private final PluginUriCustomizer pluginUriCustomizer;
    private final TokenBucket logMessageRateLimiter;

    public PublishTaskFactoryImpl(HttpClient httpClient, RequestSigner requestSigner, PluginUriResolver pluginUriResolver, UserManager userManager, PluginUriCustomizer pluginUriCustomizer)
    {
        this.httpClient = httpClient;
        this.requestSigner = requestSigner;
        this.pluginUriResolver = pluginUriResolver;
        this.userManager = userManager;
        this.pluginUriCustomizer = pluginUriCustomizer;
        // The token bucket has a max size of 5 and gains one token per minute. So you can log once per minute on average.
        // Or up to 5 times in one minute if you haven't logged in a few minutes.
        this.logMessageRateLimiter = new TokenBucket(1, ONE_MINUTE, 5);
    }

    @Override
    public PublishTask getPublishTask(WebHookEvent webHookEvent, WebHookListener listener)
    {
        return new PublishTaskImpl(
                httpClient,
                requestSigner,
                logMessageRateLimiter,
                listener,
                getListenerUri(webHookEvent, listener),
                getUserName(),
                listener.getConsumableBodyJson(webHookEvent.getJson())
        );
    }

    private URI getListenerUri(WebHookEvent webHookEvent, WebHookListener listener)
    {
        Optional<URI> uri = pluginUriResolver.getUri(listener.getPluginKey(), listener.getPath());
        if (uri.isPresent())
        {
            return pluginUriCustomizer.customizeURI(listener.getPluginKey(), uri.get(), webHookEvent);
        }
        else
        {
            logger.error("Could not resolve uri for event '{}' and listener '{}'", webHookEvent, listener);
            throw new RuntimeException("Could not resolve uri for event " + webHookEvent + " and listener " + listener);
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
        private final WebHookListener listener;
        private final TokenBucket logMessageRateLimiter;
        private final URI uri;
        private final String userName;
        private final String body;

        PublishTaskImpl(HttpClient httpClient,
                        RequestSigner requestSigner,
                        TokenBucket logMessageRateLimiter,
                        WebHookListener listener,
                        URI uri,
                        String userName,
                        String body)
        {

            this.httpClient = checkNotNull(httpClient);
            this.requestSigner = checkNotNull(requestSigner);
            this.logMessageRateLimiter = checkNotNull(logMessageRateLimiter);
            this.listener = checkNotNull(listener);
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
                    .setAttribute("pluginKey", listener.getPluginKey());

            requestSigner.sign(listener.getPluginKey(), request);
            request.post().transform().clientError(new Function<Response, Object>() {
                @Override
                public Object apply(Response response) {
                    if (logMessageRateLimiter.getToken())
                    {
                        logger.warn("Client error - {} when posting to web hook at '{}', body is:\n{}", new Object[]{response.getStatusCode(), uri, body});
                    }
                    return null;
                }
            }).serverError(new Function<Response, Object>() {
                @Override
                public Object apply(Response response) {
                    if (logMessageRateLimiter.getToken())
                    {
                        logger.warn("Server error - {} when posting to web hook at '{}', body is:\n{}", new Object[]{response.getStatusCode(), uri, body});
                    }
                    return null;
                }
            }).otherwise(new Function<Throwable, Object>()
            {
                @Override
                public Object apply(Throwable throwable)
                {
                    if (logMessageRateLimiter.getToken() && throwable != null)
                    {
                        logger.warn("Unable to post the information to {} due to {}\n", new Object[] {uri, throwable.getMessage()});
                    }
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
                    .add("listenerKey", listener)
                    .add("userName", userName)
                    .add("uri", uri)
                    .add("body", body)
                    .toString();
        }
    }
}
