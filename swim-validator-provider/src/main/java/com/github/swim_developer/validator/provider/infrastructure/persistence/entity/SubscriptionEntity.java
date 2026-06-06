package com.github.swim_developer.validator.provider.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider_validator_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", unique = true)
    private String subscriptionId;

    @Column(name = "topic_id")
    private String topicId;

    @Column(name = "queue_name")
    private String queueName;

    @Column(name = "status")
    private String status;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "provider_url")
    private String providerUrl;

    @Lob
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
}
