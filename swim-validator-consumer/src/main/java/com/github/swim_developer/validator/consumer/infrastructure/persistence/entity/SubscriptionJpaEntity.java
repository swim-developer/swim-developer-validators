package com.github.swim_developer.validator.consumer.infrastructure.persistence.entity;

import com.github.swim_developer.validator.core.domain.model.QualityOfService;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionJpaEntity {

    @Id
    @Column(name = "subscription_id", nullable = false, updatable = false)
    private UUID subscriptionId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "queue", nullable = false, unique = true)
    private String queue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus subscriptionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "qos")
    private QualityOfService qos;

    @Column(name = "durable")
    private Boolean durable;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "heartbeat_queue")
    private String heartbeatQueue;

    @Column(name = "subscription_end")
    private Instant subscriptionEnd;

    @Column(name = "event_scenario", columnDefinition = "TEXT")
    private List<String> eventScenario;

    @Column(name = "airport_heliport", columnDefinition = "TEXT")
    private List<String> airportHeliport;

    @Column(name = "airspace", columnDefinition = "TEXT")
    private List<String> airspace;

    @Column(name = "event_series")
    private String eventSeries;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
