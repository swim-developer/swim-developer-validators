package com.github.swim_developer.validator.core.infrastructure.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NoCacheFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("ui") || path.startsWith("admin")) {
            responseContext.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            responseContext.getHeaders().add("Pragma", "no-cache");
            responseContext.getHeaders().add("Expires", "0");
        }
    }
}
