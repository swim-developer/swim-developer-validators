package com.github.swim_developer.validator.consumer.domain.port.out;

import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Optional<SubscriptionEntity> findBySubscriptionId(UUID subscriptionId);

    List<SubscriptionEntity> findByQueue(String queue);

    List<SubscriptionEntity> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);

    List<SubscriptionEntity> findByQueueAndSubscriptionStatus(String queue, SubscriptionStatus subscriptionStatus);

    long countBySubscriptionStatus(SubscriptionStatus status);

    SubscriptionEntity save(SubscriptionEntity entity);

    void delete(SubscriptionEntity entity);

    long deleteAll();

    List<SubscriptionEntity> findAllSubscriptions();

    long count();
}
