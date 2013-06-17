package com.atlassian.webhooks.plugin.rest;

import com.atlassian.plugins.rest.common.security.jersey.AdminOnlyResourceFilter;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.plugin.ao.DelegatingWebHookListenerRegistrationParameters;
import com.atlassian.webhooks.plugin.ao.WebHookAO;
import com.atlassian.webhooks.plugin.manager.WebHookListenerManager;
import com.atlassian.webhooks.plugin.service.WebHookListenerService;
import com.atlassian.webhooks.spi.provider.WebHookListenerActionValidator;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.sun.jersey.spi.container.ResourceFilters;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.*;

@Path("webhook")
@ResourceFilters(AdminOnlyResourceFilter.class)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationResource
{
    private final UserManager userManager;
    private final WebHookListenerService webHookListenerService;
    private final WebHookListenerActionValidator webHookListenerActionValidator;

    public RegistrationResource(UserManager userManager, WebHookListenerService webHookListenerService, WebHookListenerActionValidator webHookListenerActionValidator)
    {
        this.userManager = checkNotNull(userManager);
        this.webHookListenerService = checkNotNull(webHookListenerService);
        this.webHookListenerActionValidator = checkNotNull(webHookListenerActionValidator);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(final WebHookListenerRegistration registration, @Context final UriInfo uriInfo, @DefaultValue("false") @QueryParam("ui") boolean registeredViaUI)
    {
        final MessageCollection messageCollection = webHookListenerActionValidator.validateWebHookAddition(registration);
        if (!messageCollection.isEmpty())
        {
            return status(Response.Status.BAD_REQUEST).entity(new SerializableErrorCollection(messageCollection)).build();
        }

        // TODO validate unique registrations

        final WebHookAO webHookAO = webHookListenerService.addWebHook(registration.getName(),
                registration.getUrl(),
                registration.getEvents(),
                registration.getParameters(),
                registeredViaUI ? WebHookListenerManager.WebHookListenerRegistrationMethod.UI : WebHookListenerManager.WebHookListenerRegistrationMethod.REST
        );

        final URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(webHookAO.getID())).build();
        return created(uri).entity(new WebHookListenerRegistrationResponse.Factory(userManager).create(webHookAO, uri)).build();
    }

    @GET
    @Path("{id}")
    public Response getWebHook(@PathParam ("id") final int id, @Context final UriInfo uriInfo)
    {
        final Optional<WebHookAO> webhook = webHookListenerService.getWebHook(id);
        if (!webhook.isPresent())
        {
            return status(Response.Status.NOT_FOUND).build();
        }
        else
        {
            final URI self = uriInfo.getAbsolutePath();
            return ok(new WebHookListenerRegistrationResponse.Factory(userManager).create(webhook.get(), self)).build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteWebHook(@PathParam("id") final int id)
    {
        final Optional<WebHookAO> webHook = webHookListenerService.getWebHook(id);
        if (!webHook.isPresent())
        {
            return status(Response.Status.NOT_FOUND).build();
        }
        final MessageCollection messageCollection =
                webHookListenerActionValidator.validateWebHookDeletion(new DelegatingWebHookListenerRegistrationParameters(webHook.get()));

        try
        {
            if (messageCollection.isEmpty())
            {
                webHookListenerService.removeWebHook(id);
                return noContent().build();
            }
            else
            {
                return status(Status.CONFLICT).entity(new SerializableErrorCollection(messageCollection)).build();
            }
        }
        catch (IllegalArgumentException iae)
        {
            return status(Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam ("id") final int id, final WebHookListenerRegistration registration, @Context final UriInfo uriInfo)
    {
        final MessageCollection messageCollection = webHookListenerActionValidator.validateWebHookUpdate(registration);

        if (!messageCollection.isEmpty())
        {
            return status(Status.BAD_REQUEST).entity(new SerializableErrorCollection(messageCollection)).build();
        }

        final Optional<WebHookAO> webHookToUpdate = webHookListenerService.getWebHook(id);

        try
        {
            final WebHookAO webhookDao = webHookListenerService.updateWebHook(id,
                    registration.getName(),
                    registration.getUrl(),
                    registration.getEvents(),
                    registration.getParameters(),
                    webHookToUpdate.get().isEnabled());
            final URI self = uriInfo.getAbsolutePath();
            return ok(new WebHookListenerRegistrationResponse.Factory(userManager).create(webhookDao, self)).build();
        }
        catch (IllegalArgumentException e)
        {
            return status(Status.NOT_FOUND).build();
        }
    }

    @GET
    public Response getAllWebHooks(@Context final UriInfo uriInfo)
    {
        final Iterable<WebHookAO> webHooks = webHookListenerService.getAllWebHooks();
        return ok(
                Iterables.transform(webHooks, new Function<WebHookAO, Object>()
                {
                    @Override
                    public WebHookListenerRegistrationResponse apply(final WebHookAO webHook)
                    {
                        final URI self = uriInfo.getAbsolutePathBuilder().path(String.valueOf(webHook.getID())).build();
                        return new WebHookListenerRegistrationResponse.Factory(userManager).create(webHook, self);
                    }
                })
        ).build();
    }

    @PUT
    @Path("{id}/enabled")
    public Response enableWebHook(@PathParam("id") final int id, final String enabled)
    {
        boolean enabledFlag = Boolean.parseBoolean(enabled);

        final Optional<WebHookAO> enablementResult = webHookListenerService.enableWebHook(id, enabledFlag);
        if (enablementResult.isPresent())
        {
            return ok(enablementResult.get().isEnabled()).build();
        }
        return status(Status.NOT_FOUND).build();
    }

}
