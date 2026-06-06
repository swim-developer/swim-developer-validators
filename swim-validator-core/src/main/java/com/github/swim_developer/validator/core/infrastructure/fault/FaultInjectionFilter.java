package com.github.swim_developer.validator.core.infrastructure.fault;

import com.github.swim_developer.validator.core.infrastructure.rest.dto.ErrorResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Provider
public class FaultInjectionFilter implements ContainerRequestFilter {

    private final FaultInjectionService faultInjectionService;

    @Inject
    public FaultInjectionFilter(FaultInjectionService faultInjectionService) {
        this.faultInjectionService = faultInjectionService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        if (path.startsWith("admin/") || path.startsWith("ui/") || path.startsWith("q/")) {
            return;
        }

        Optional<FaultConfig> faultOpt = faultInjectionService.findMatchingFault(method, path);
        if (faultOpt.isEmpty()) {
            return;
        }

        FaultConfig fault = faultOpt.get();

        if (faultInjectionService.shouldDrop(fault)) {
            log.warn("Fault injection: DROP - {} {}", method, path);
            requestContext.abortWith(Response.status(503).entity(
                    new ErrorResponse("FAULT_INJECTION_DROP", "Request dropped by fault injection",
                            "Simulated random drop (dropRate: " + fault.dropRate() + "%)")).build());
            return;
        }

        if (fault.delayMs() != null && fault.delayMs() > 0) {
            try {
                log.warn("Fault injection: DELAY {}ms - {} {}", fault.delayMs(), method, path);
                Thread.sleep(fault.delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (fault.httpStatus() != null && fault.httpStatus() >= 400) {
            log.warn("Fault injection: STATUS {} - {} {}", fault.httpStatus(), method, path);
            requestContext.abortWith(Response.status(fault.httpStatus()).entity(
                    new ErrorResponse("FAULT_INJECTION_ERROR", "Simulated error response",
                            "HTTP " + fault.httpStatus() + " injected by fault injection")).build());
        }
    }
}
