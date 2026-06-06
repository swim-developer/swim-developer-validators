package com.github.swim_developer.validator.consumer.domain.model;

import com.github.swim_developer.validator.core.domain.model.QualityOfService;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionEntity {

    private UUID subscriptionId;
    private String topic;
    private String queue;
    private SubscriptionStatus subscriptionStatus;
    private QualityOfService qos;
    private Boolean durable;
    private String providerName;
    private String heartbeatQueue;
    private Instant subscriptionEnd;
    private List<String> eventScenario;
    private List<String> airportHeliport;
    private List<String> airspace;
    private String eventSeries;
    private String publisher;
    private String description;
    private String comment;

    public static SubscriptionEntity create(String topic, String queue, SubscriptionStatus status,
                                            QualityOfService qos, Boolean durable) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSubscriptionId(UUID.randomUUID());
        entity.setTopic(topic);
        entity.setQueue(queue);
        entity.setSubscriptionStatus(status);
        entity.setQos(qos);
        entity.setDurable(durable);
        return entity;
    }
}
