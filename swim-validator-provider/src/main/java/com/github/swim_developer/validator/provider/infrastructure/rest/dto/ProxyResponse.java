package com.github.swim_developer.validator.provider.infrastructure.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ProxyResponse(int status, String contentType, String body) {}
