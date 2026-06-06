package com.github.swim_developer.validator.provider.domain.model;



public record AssertionResult(
        String name,
        boolean passed,
        String expected,
        String actual,
        String message) {}
