package com.github.swim_developer.validator.provider.infrastructure.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ProxyRequest(String method, String url, String token, String body, String contentType) {}
