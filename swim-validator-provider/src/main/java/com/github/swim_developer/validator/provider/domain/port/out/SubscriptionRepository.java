package com.github.swim_developer.validator.provider.domain.port.out;

import com.github.swim_developer.validator.provider.domain.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription insert(Subscription entity);
    Subscription update(Subscription entity);
    List<Subscription> findByUsername(String username);
    Optional<Subscription> findBySubscriptionId(String subscriptionId);
    long countByUsernameAndStatusNot(String username, String status);
}
