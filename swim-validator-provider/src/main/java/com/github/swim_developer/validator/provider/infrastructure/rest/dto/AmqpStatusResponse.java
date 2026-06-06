package com.github.swim_developer.validator.provider.infrastructure.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;

@RegisterForReflection
public record AmqpStatusResponse(
    boolean authenticated,
    String userId,
    String username,
    boolean connected,
    String host,
    int port,
    String brokerUsername,
    int activeReceivers,
    long activeSubscriptions,
    Map<String, Boolean> receivers
) {
    public static AmqpStatusResponse unauthenticated(String host, int port) {
        return new AmqpStatusResponse(false, null, null, false, host, port, null, 0, 0, Map.of());
    }
}
