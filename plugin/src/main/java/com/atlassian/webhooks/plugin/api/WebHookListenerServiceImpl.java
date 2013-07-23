package com.atlassian.webhooks.plugin.api;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.api.provider.WebHookListenerServiceResponse;
import com.atlassian.webhooks.plugin.event.WebHookEventDispatcher;
import com.atlassian.webhooks.plugin.store.WebHookListenerCachingStore;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.atlassian.webhooks.spi.provider.WebHookListenerRegistrationParameters;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.symmetricDifference;

/**
 * Implementation of {@link WebHookListenerService}. Wraps the InternalWebHookListenerService and WebHookAO.
 * TODO use API in plugin as well and add verification here.
 */
public class WebHookListenerServiceImpl implements WebHookListenerService
{
    private final WebHookListenerCachingStore webHookListenerCachingStore;
    private final WebHookListenerActionValidator webHookListenerActionValidator;
    private final WebHookEventDispatcher webHookEventDispatcher;
    private final I18nResolver i18n;

    public WebHookListenerServiceImpl(WebHookListenerCachingStore webHookListenerCachingStore,
            WebHookListenerActionValidator webHookListenerActionValidator,
            WebHookEventDispatcher webHookEventDispatcher,
            I18nResolver i18nResolver)
    {
        this.i18n = checkNotNull(i18nResolver);
        this.webHookEventDispatcher = checkNotNull(webHookEventDispatcher);
        this.webHookListenerActionValidator = checkNotNull(webHookListenerActionValidator);
        this.webHookListenerCachingStore = checkNotNull(webHookListenerCachingStore);
    }

    @Override
    public Iterable<WebHookListenerParameters> getAllWebHookListeners()
    {
        return webHookListenerCachingStore.getAllWebHookListeners();
    }

    @Override
    public Optional<WebHookListenerParameters> getWebHookListener(Integer id)
    {
        return webHookListenerCachingStore.getWebHookListener(id);
    }

    @Override
    public WebHookListenerServiceResponse registerWebHookListener(WebHookListenerRegistrationParameters registrationParameters)
    {
        checkWebHookListenerParameters(registrationParameters);
        final MessageCollection messageCollection =
                webHookListenerActionValidator.validateWebHookRegistration(registrationParameters);
        if (!messageCollection.isEmpty())
        {
            return new WebHookListenerServiceResponse(messageCollection);
        }
        final WebHookListenerParameters registeredListener = webHookListenerCachingStore.registerWebHookListener(
                registrationParameters.getName(), registrationParameters.getUrl(), registrationParameters.getEvents(),
                registrationParameters.getParameters(), null);
        webHookEventDispatcher.webHookCreated(registeredListener);
        return new WebHookListenerServiceResponse(registeredListener);
    }

    /**
     *
     * @param id Id of the WebHook listener to updateWebHookListener.
     * @param registrationParameters The parameters of WebHook listener to update.
     * @return updated WebHookListener or collection with error messages.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    @Override
    public WebHookListenerServiceResponse updateWebHookListener(int id, WebHookListenerRegistrationParameters registrationParameters)
            throws IllegalArgumentException
    {
        checkWebHookListenerParameters(registrationParameters);
        validateUniqueRegistration(id, registrationParameters);
        final MessageCollection messageCollection =
                webHookListenerActionValidator.validateWebHookUpdate(registrationParameters);
        if (!messageCollection.isEmpty())
        {
            return new WebHookListenerServiceResponse(messageCollection);
        }
        final WebHookListenerParameters updatedListener =
                webHookListenerCachingStore.updateWebHookListener(id, registrationParameters.getName(), registrationParameters.getUrl(),
                        registrationParameters.getEvents(), registrationParameters.getParameters(), registrationParameters.isEnabled());
        webHookEventDispatcher.webHookEdited(updatedListener);
        return new WebHookListenerServiceResponse(updatedListener);
    }

    /**
     *
     * @param id Id of WebHookListener to remove.
     * @throws IllegalArgumentException when WebHookListener with given id is not found.
     * @return collection of messages
     */
    @Override
    public MessageCollection deleteWebHookListener(int id)
    {
        final Optional<WebHookListenerParameters> webHookListener = getWebHookListener(id);
        if (!webHookListener.isPresent())
        {
            throw new IllegalArgumentException(String.format("WebHookListener with id %d not found", id));
        }
        final MessageCollection messageCollection = webHookListenerActionValidator.validateWebHookRemoval(webHookListener.get());
        if (messageCollection.isEmpty())
        {
            webHookListenerCachingStore.removeWebHookListener(id);
        }
        return messageCollection;
    }

    @Override
    public Optional<WebHookListenerParameters> enableWebHookListener(final int id, final boolean flag)
    {
        Optional<WebHookListenerParameters> webHook = webHookListenerCachingStore.enableWebHook(id, flag);
        if (webHook.isPresent())
        {
            if (flag)
            {
                webHookEventDispatcher.webHookEnabled(webHook.get());
            }
            else
            {
                webHookEventDispatcher.webHookDisabled(webHook.get());
            }
        }
        return webHook;
    }

    /**
     * Verifies WebHookRegistrationParameters.
     * @throws NullPointerException if any of required parameters is null.
     * @param webHookListenerParameters registration parameters to verify.
     */
    private void checkWebHookListenerParameters(WebHookListenerRegistrationParameters webHookListenerParameters)
    {
        checkNotNull(webHookListenerParameters.getName(), i18n.getText("webhooks.empty.field", "name"));
        checkNotNull(webHookListenerParameters.getUrl(), i18n.getText("webhooks.empty.field", "url"));
        checkNotNull(webHookListenerParameters.getEvents(), i18n.getText("webhooks.empty.field", "events"));
    }

    /**
     * @param webHookListenerParameters registration parameters to verify.
     * @param id of the listener to update.
     * @throws NonUniqueRegistrationException if the listener with such parameters already exists.
     */
    private void validateUniqueRegistration(int id, WebHookListenerRegistrationParameters webHookListenerParameters)
    {
        final Optional<WebHookListenerParameters> exists = findWebHookListener(id, webHookListenerParameters.getUrl(), webHookListenerParameters.getEvents(), webHookListenerParameters.getParameters());
        if (exists.isPresent())
        {
            throw new NonUniqueRegistrationException(i18n.getText("webhooks.duplicate.registration"), exists.get().getId());
        }
    }

    private Optional<WebHookListenerParameters> findWebHookListener(final Integer id, final String url, final Iterable<String> events, final String parameters)
    {
        final WebHookListenerParameters webHookListenerParameters = Iterables.find(webHookListenerCachingStore.getAllWebHookListeners(), new Predicate<WebHookListenerParameters>()
        {
            @Override
            public boolean apply(final WebHookListenerParameters listenerParameters)
            {
                // You can't be a duplicate of yourself.
                if (id != null && id.equals(listenerParameters.getId()))
                {
                    return false;
                }

                if (!StringUtils.equals(url, listenerParameters.getUrl()))
                {
                    return false;
                }
                if (!StringUtils.equals(parameters, listenerParameters.getParameters()))
                {
                    return false;
                }
                return symmetricDifference(events != null ? copyOf(events) : of(), copyOf(listenerParameters.getEvents())).isEmpty();
            }
        }, null);
        return Optional.fromNullable(webHookListenerParameters);
    }

}
