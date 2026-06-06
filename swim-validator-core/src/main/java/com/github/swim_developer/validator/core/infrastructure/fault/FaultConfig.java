package com.github.swim_developer.validator.core.infrastructure.fault;

import java.time.Instant;
import java.util.regex.Pattern;

public record FaultConfig(
    String pathPattern,
    String httpMethod,
    Integer httpStatus,
    Long delayMs,
    Double dropRate,
    Instant expiresAt
) {
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean matches(String method, String path) {
        boolean methodMatch = httpMethod == null || httpMethod.equalsIgnoreCase(method);
        boolean pathMatch = pathPattern == null
                || (path != null && Pattern.compile(pathPattern).matcher(path).matches());
        return methodMatch && pathMatch;
    }
}
