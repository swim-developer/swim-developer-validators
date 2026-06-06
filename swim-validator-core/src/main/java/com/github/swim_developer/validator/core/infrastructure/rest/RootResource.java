package com.github.swim_developer.validator.core.infrastructure.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("/")
public class RootResource {

    private static final String UI_PATH = "/ui";

    @GET
    public Response redirectToUi() {
        return Response.seeOther(UriBuilder.fromPath(UI_PATH).build()).build();
    }
}
