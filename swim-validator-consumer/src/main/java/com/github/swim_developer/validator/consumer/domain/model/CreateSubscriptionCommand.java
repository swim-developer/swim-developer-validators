package com.github.swim_developer.validator.consumer.domain.model;

import com.github.swim_developer.validator.core.domain.model.QualityOfService;

import java.util.List;

public record CreateSubscriptionCommand(
        String topic,
        String queueName,
        QualityOfService qos,
        Boolean durable,
        List<String> eventScenario,
        List<String> airportHeliport,
        List<String> airspace,
        String eventSeries,
        String publisher,
        String description,
        String comment) {
}
