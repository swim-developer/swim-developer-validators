package com.github.swim_developer.validator.provider.infrastructure.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SubscriptionSummary(
    String subscriptionId,
    String queueName,
    String status,
    String topicId,
    String createdAt,
    boolean listening,
    long messageCount,
    String filters
) {}
