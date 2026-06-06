package com.github.swim_developer.validator.provider.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserAmqpConfig {

    private Long id;
    private String userId;
    private String username;
    private String amqpHost;
    private int amqpPort;
    private String brokerUsername;
    private LocalDateTime updatedAt;
}
