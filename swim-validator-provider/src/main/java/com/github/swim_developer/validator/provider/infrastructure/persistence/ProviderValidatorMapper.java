package com.github.swim_developer.validator.provider.infrastructure.persistence;

import com.github.swim_developer.validator.provider.domain.model.Subscription;
import com.github.swim_developer.validator.provider.domain.model.UserAmqpConfig;
import com.github.swim_developer.validator.provider.infrastructure.persistence.entity.SubscriptionEntity;
import com.github.swim_developer.validator.provider.infrastructure.persistence.entity.UserAmqpConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProviderValidatorMapper {

    public SubscriptionEntity toEntity(Subscription domain) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setId(domain.getId());
        entity.setSubscriptionId(domain.getSubscriptionId());
        entity.setTopicId(domain.getTopicId());
        entity.setQueueName(domain.getQueueName());
        entity.setStatus(domain.getStatus());
        entity.setUsername(domain.getUsername());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setProviderUrl(domain.getProviderUrl());
        entity.setRequestBody(domain.getRequestBody());
        return entity;
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        Subscription domain = new Subscription();
        domain.setId(entity.getId());
        domain.setSubscriptionId(entity.getSubscriptionId());
        domain.setTopicId(entity.getTopicId());
        domain.setQueueName(entity.getQueueName());
        domain.setStatus(entity.getStatus());
        domain.setUsername(entity.getUsername());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setProviderUrl(entity.getProviderUrl());
        domain.setRequestBody(entity.getRequestBody());
        return domain;
    }

    public UserAmqpConfigEntity toEntity(UserAmqpConfig domain) {
        UserAmqpConfigEntity entity = new UserAmqpConfigEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setUsername(domain.getUsername());
        entity.setAmqpHost(domain.getAmqpHost());
        entity.setAmqpPort(domain.getAmqpPort());
        entity.setBrokerUsername(domain.getBrokerUsername());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public UserAmqpConfig toDomain(UserAmqpConfigEntity entity) {
        UserAmqpConfig domain = new UserAmqpConfig();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setUsername(entity.getUsername());
        domain.setAmqpHost(entity.getAmqpHost());
        domain.setAmqpPort(entity.getAmqpPort());
        domain.setBrokerUsername(entity.getBrokerUsername());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
