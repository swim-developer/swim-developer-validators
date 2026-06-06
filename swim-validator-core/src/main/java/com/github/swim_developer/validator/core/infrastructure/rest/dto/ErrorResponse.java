package com.github.swim_developer.validator.core.infrastructure.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error response structure")
public record ErrorResponse(
    @Schema(description = "Error code")
    String code,

    @Schema(description = "Error message")
    String message,

    @Schema(description = "Additional error details")
    String details
) {}
