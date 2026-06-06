package com.github.swim_developer.validator.core.infrastructure.fault;

import java.time.Instant;

public record FaultStatus(
        String id,
        String pathPattern,
        String httpMethod,
        Integer httpStatus,
        Long delayMs,
        Double dropRate,
        Instant expiresAt,
        boolean expired) {}
