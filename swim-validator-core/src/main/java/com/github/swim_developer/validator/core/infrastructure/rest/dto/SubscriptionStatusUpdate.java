package com.github.swim_developer.validator.core.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to update subscription status (JSON field subscription_status, same as swim-dnotam-provider). "
        + "Allowed values: ACTIVE, PAUSED, SUSPENDED, TERMINATED, DELETED, INVALID.")
public record SubscriptionStatusUpdate(
    @NotNull
    @JsonProperty("subscription_status")
    SubscriptionStatus subscriptionStatus
) {}
