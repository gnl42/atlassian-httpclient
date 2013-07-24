package com.atlassian.webhooks.plugin.rest;

import com.atlassian.plugins.rest.common.security.jersey.AdminOnlyResourceFilter;
import com.atlassian.sal.api.message.MessageCollection;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.provider.WebHookListenerService;
import com.atlassian.webhooks.api.provider.WebHookListenerServiceResponse;
import com.atlassian.webhooks.spi.provider.WebHookListenerParameters;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.sun.jersey.spi.container.ResourceFilters;

import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path("webhook")
@ResourceFilters(AdminOnlyResourceFilter.class)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationResource
{
    private final UserManager userManager;
    private final WebHookListenerService webHookListenerService;

    public RegistrationResource(UserManager userManager, final WebHookListenerService webHookListenerService)
    {
        this.webHookListenerService = checkNotNull(webHookListenerService);
        this.userManager = checkNotNull(userManager);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(final WebHookListenerRegistration registration, @Context final UriInfo uriInfo, @DefaultValue("false") @QueryParam("ui") boolean registeredViaUI)
    {
        try
        {
            final WebHookListenerServiceResponse webHookListenerServiceResponse =
                    webHookListenerService.registerWebHookListener(registration, registeredViaUI ? WebHookListenerService.RegistrationMethod.UI : WebHookListenerService.RegistrationMethod.REST);

            if (!webHookListenerServiceResponse.getMessageCollection().isEmpty())
            {
                return status(Response.Status.BAD_REQUEST).entity(new SerializableErrorCollection(webHookListenerServiceResponse.getMessageCollection())).build();
            }
            else
            {
                final WebHookListenerParameters registeredListener = webHookListenerServiceResponse.getRegisteredListener().or(LISTENER_PARAMETERS_SUPPLIER);
                final URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(webHookListenerServiceResponse.getRegisteredListener().get().getId())).build();
                return created(uri).entity(new WebHookListenerRegistrationResponse.Factory(userManager).create(registeredListener, uri)).build();
            }
        }
        catch (NullPointerException npe)
        {
            return status(Status.BAD_REQUEST).entity(new SerializableErrorCollection(npe)).build();
        }
        catch (IllegalArgumentException e)
        {
            return status(Status.BAD_REQUEST).entity(new SerializableErrorCollection(e)).build();
        }
        catch (WebHookListenerService.NonUniqueRegistrationException e)
        {
            return status(Status.CONFLICT).entity(new SerializableErrorCollection(e)).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam ("id") final int id, final WebHookListenerRegistration registration, @Context final UriInfo uriInfo)
    {
        try
        {
            final WebHookListenerServiceResponse webHookListenerServiceResponse =
                    webHookListenerService.updateWebHookListener(id, registration);

            if (!webHookListenerServiceResponse.getMessageCollection().isEmpty())
            {
                return status(Response.Status.BAD_REQUEST).entity(new SerializableErrorCollection(webHookListenerServiceResponse.getMessageCollection())).build();
            }
            else
            {
                final WebHookListenerParameters registeredListener = webHookListenerServiceResponse.getRegisteredListener().or(LISTENER_PARAMETERS_SUPPLIER);
                final URI self = uriInfo.getAbsolutePath();
                return ok(new WebHookListenerRegistrationResponse.Factory(userManager).create(registeredListener, self)).build();
            }
        }
        catch (IllegalArgumentException e)
        {
            return status(Status.NOT_FOUND).entity(new SerializableErrorCollection(e)).build();
        }
        catch (NullPointerException npe)
        {
            return status(Status.BAD_REQUEST).entity(new SerializableErrorCollection(npe)).build();
        }
        catch (WebHookListenerService.NonUniqueRegistrationException e)
        {
            return status(Status.CONFLICT).entity(new SerializableErrorCollection(e)).build();
        }
    }

    @GET
    @Path("{id}")
    public Response getWebHook(@PathParam ("id") final int id, @Context final UriInfo uriInfo)
    {
        final Optional<WebHookListenerParameters> webhook = webHookListenerService.getWebHookListener(id);
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
        try
        {
            final MessageCollection messageCollection = webHookListenerService.deleteWebHookListener(id);
            if (messageCollection.isEmpty())
            {
                return noContent().build();
            }
            else
            {
                return status(Status.CONFLICT).entity(new SerializableErrorCollection(messageCollection)).build();
            }
        }
        catch (IllegalArgumentException e)
        {
            return status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    public Response getAllWebHooks(@Context final UriInfo uriInfo)
    {
        final Iterable<WebHookListenerParameters> allWebHookListeners = webHookListenerService.getAllWebHookListeners();
        return ok(
                Iterables.transform(allWebHookListeners, new Function<WebHookListenerParameters, Object>()
                {
                    @Override
                    public WebHookListenerRegistrationResponse apply(final WebHookListenerParameters webHook)
                    {
                        final URI self = uriInfo.getAbsolutePathBuilder().path(String.valueOf(webHook.getId())).build();
                        return new WebHookListenerRegistrationResponse.Factory(userManager).create(webHook, self);
                    }
                })
        ).build();
    }

    @PUT
    @Path("{id}/enabled")
    public Response enableWebHook(@PathParam("id") int id, final String enabled)
    {
        boolean enabledFlag = Boolean.parseBoolean(enabled);

        final Optional<WebHookListenerParameters> enablementResult = webHookListenerService.enableWebHookListener(id, enabledFlag);
        if (enablementResult.isPresent())
        {
            return ok(enablementResult.get().isEnabled()).build();
        }
        return status(Status.NOT_FOUND).build();
    }

    private static final Supplier<WebHookListenerParameters> LISTENER_PARAMETERS_SUPPLIER = new Supplier<WebHookListenerParameters>()
    {
        @Override
        public WebHookListenerParameters get()
        {
            throw new WebApplicationException(Response.serverError().build());
        }
    };
}
