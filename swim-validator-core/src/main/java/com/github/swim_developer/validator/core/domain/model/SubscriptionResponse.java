package com.github.swim_developer.validator.core.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SubscriptionResponse(
    String topic,
    UUID subscriptionId,
    String queue,
    SubscriptionStatus subscriptionStatus,
    QualityOfService qos,
    Boolean durable,
    Instant subscriptionEnd,
    String providerName,
    String heartbeatQueue,
    List<String> eventScenario,
    List<String> airportHeliport,
    List<String> airspace,
    String eventSeries,
    String publisher,
    String description,
    String comment
) {}
