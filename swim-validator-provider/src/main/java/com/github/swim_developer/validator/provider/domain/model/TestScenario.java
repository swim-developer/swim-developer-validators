package com.github.swim_developer.validator.provider.domain.model;



public record TestScenario(
    String id,
    String name,
    String category,
    String description,
    String procedure,
    String verification,
    TestStatus implementationStatus
) {}
