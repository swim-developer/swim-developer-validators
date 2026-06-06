package com.github.swim_developer.validator.core.infrastructure.fault;

public record FaultRequest(
        String pathPattern,
        String httpMethod,
        Integer httpStatus,
        Long delayMs,
        Double dropRate,
        Integer durationSeconds) {}
