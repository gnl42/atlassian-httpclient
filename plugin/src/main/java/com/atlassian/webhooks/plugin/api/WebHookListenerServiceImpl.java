package com.atlassian.webhooks.plugin.api;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
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
 * Implementation of {@link WebHookListenerService}.
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
    public WebHookListenerServiceResponse registerWebHookListener(
            final WebHookListenerRegistrationParameters registrationParameters,
            final RegistrationMethod registrationMethod)
    {
        validateUniqueRegistration(null, registrationParameters);
        final MessageCollection messageCollection =
                webHookListenerActionValidator.validateWebHookRegistration(registrationParameters);
        messageCollection.addAll(checkWebHookListenerParameters(registrationParameters).getMessages());
        if (!messageCollection.isEmpty())
        {
            return new WebHookListenerServiceResponse(messageCollection);
        }
        final WebHookListenerParameters registeredListener = webHookListenerCachingStore.registerWebHookListener(
                registrationParameters.getName(), registrationParameters.getUrl(), registrationParameters.getEvents(),
                registrationParameters.getParameters(), registrationMethod);
        webHookEventDispatcher.webHookCreated(registeredListener);
        return new WebHookListenerServiceResponse(registeredListener);
    }

    @Override
    public WebHookListenerServiceResponse registerWebHookListener(WebHookListenerRegistrationParameters registrationParameters)
    {
        return registerWebHookListener(registrationParameters, RegistrationMethod.SERVICE);
    }

    @Override
    public WebHookListenerServiceResponse updateWebHookListener(int id, WebHookListenerRegistrationParameters registrationParameters)
            throws IllegalArgumentException
    {
        validateUniqueRegistration(id, registrationParameters);
        final MessageCollection messageCollection =
                webHookListenerActionValidator.validateWebHookUpdate(registrationParameters);
        messageCollection.addAll(checkWebHookListenerParameters(registrationParameters).getMessages());
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
        webHookEventDispatcher.webHookDeleted(webHookListener.get());
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
     * @param webHookListenerParameters registration parameters to verify.
     */
    private WebHookListenerActionValidator.ErrorMessageCollection checkWebHookListenerParameters(WebHookListenerRegistrationParameters webHookListenerParameters)
    {
        WebHookListenerActionValidator.ErrorMessageCollection messageCollection = new WebHookListenerActionValidator.ErrorMessageCollection();
        if (StringUtils.isEmpty(webHookListenerParameters.getName()))
        {
            messageCollection.addMessage(createErrorMessageForRequiredParameter("name"));
        }
        if (StringUtils.isEmpty(webHookListenerParameters.getUrl()))
        {
            messageCollection.addMessage(createErrorMessageForRequiredParameter("url"));
        }
        if (webHookListenerParameters.getEvents() == null)
        {
            messageCollection.addMessage(createErrorMessageForRequiredParameter("events"));
        }
        return messageCollection;
    }

    /**
     * @param webHookListenerParameters registration parameters to verify.
     * @param id of the listener to update.
     * @throws NonUniqueRegistrationException if the listener with such parameters already exists.
     */
    private void validateUniqueRegistration(Integer id, WebHookListenerRegistrationParameters webHookListenerParameters)
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

    /**
     * @param parameterName for which error message is created.
     * @return error message saying that required field is empty.
     */
    private Message createErrorMessageForRequiredParameter(String parameterName)
    {
        return new WebHookListenerActionValidator.ErrorMessage(parameterName, new String[] {i18n.getText("webhooks.empty.field", parameterName)});
    }
}
