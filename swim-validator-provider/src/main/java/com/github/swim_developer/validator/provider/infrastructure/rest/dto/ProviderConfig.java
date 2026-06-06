package com.github.swim_developer.validator.provider.infrastructure.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record ProviderConfig(List<String> apiUrls) {}
