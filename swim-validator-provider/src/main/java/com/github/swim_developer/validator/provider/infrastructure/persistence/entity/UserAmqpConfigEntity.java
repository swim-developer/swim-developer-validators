package com.github.swim_developer.validator.provider.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_amqp_configs")
@Getter
@Setter
@NoArgsConstructor
public class UserAmqpConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "amqp_host")
    private String amqpHost;

    @Column(name = "amqp_port")
    private int amqpPort;

    @Column(name = "broker_username")
    private String brokerUsername;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
