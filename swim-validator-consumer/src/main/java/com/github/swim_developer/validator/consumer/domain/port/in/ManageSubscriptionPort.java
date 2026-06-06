package com.github.swim_developer.validator.consumer.domain.port.in;

import com.github.swim_developer.validator.consumer.domain.model.CreateSubscriptionCommand;
import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.core.domain.model.SubscriptionResponse;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;

import java.util.List;
import java.util.Optional;

public interface ManageSubscriptionPort {
    SubscriptionResponse createSubscription(CreateSubscriptionCommand command);
    SubscriptionResponse updateSubscriptionStatus(String subscriptionId, SubscriptionStatus newStatus);
    Optional<SubscriptionResponse> getSubscriptionDetails(String subscriptionId);
    List<SubscriptionResponse> listSubscriptions(String queueName, SubscriptionStatus status);
    void deleteSubscription(String subscriptionId);
    Optional<SubscriptionResponse> renewSubscription(String subscriptionId);
    String generateQueueName(String userId);
    List<SubscriptionEntity> listAll();
    long countAll();
    long countActive();
}
