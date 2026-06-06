package com.github.swim_developer.validator.consumer.infrastructure.persistence;

import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.infrastructure.persistence.entity.SubscriptionJpaEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsumerSubscriptionMapper {

    public SubscriptionJpaEntity toJpa(SubscriptionEntity domain) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.setSubscriptionId(domain.getSubscriptionId());
        entity.setTopic(domain.getTopic());
        entity.setQueue(domain.getQueue());
        entity.setSubscriptionStatus(domain.getSubscriptionStatus());
        entity.setQos(domain.getQos());
        entity.setDurable(domain.getDurable());
        entity.setProviderName(domain.getProviderName());
        entity.setHeartbeatQueue(domain.getHeartbeatQueue());
        entity.setSubscriptionEnd(domain.getSubscriptionEnd());
        entity.setEventScenario(domain.getEventScenario());
        entity.setAirportHeliport(domain.getAirportHeliport());
        entity.setAirspace(domain.getAirspace());
        entity.setEventSeries(domain.getEventSeries());
        entity.setPublisher(domain.getPublisher());
        entity.setDescription(domain.getDescription());
        entity.setComment(domain.getComment());
        return entity;
    }

    public SubscriptionEntity toDomain(SubscriptionJpaEntity entity) {
        SubscriptionEntity domain = new SubscriptionEntity();
        domain.setSubscriptionId(entity.getSubscriptionId());
        domain.setTopic(entity.getTopic());
        domain.setQueue(entity.getQueue());
        domain.setSubscriptionStatus(entity.getSubscriptionStatus());
        domain.setQos(entity.getQos());
        domain.setDurable(entity.getDurable());
        domain.setProviderName(entity.getProviderName());
        domain.setHeartbeatQueue(entity.getHeartbeatQueue());
        domain.setSubscriptionEnd(entity.getSubscriptionEnd());
        domain.setEventScenario(entity.getEventScenario());
        domain.setAirportHeliport(entity.getAirportHeliport());
        domain.setAirspace(entity.getAirspace());
        domain.setEventSeries(entity.getEventSeries());
        domain.setPublisher(entity.getPublisher());
        domain.setDescription(entity.getDescription());
        domain.setComment(entity.getComment());
        return domain;
    }
}
