package com.github.swim_developer.validator.provider.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    private Long id;
    private String subscriptionId;
    private String topicId;
    private String queueName;
    private String status;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String providerUrl;
    private String requestBody;
}
