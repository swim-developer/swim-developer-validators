package com.github.swim_developer.validator.provider.domain.model;


import java.time.Instant;
import java.util.List;


public record TestResult(
        String scenarioId,
        String scenarioName,
        TestOutcome outcome,
        Instant timestamp,
        Long durationMs,
        String errorMessage,
        List<AssertionResult> assertions,
        String providerUrl,
        String requestBody,
        String responseBody,
        Integer httpStatus) {}
