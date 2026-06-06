package com.github.swim_developer.validator.consumer.application.usecase;

import com.github.swim_developer.validator.consumer.domain.model.CreateSubscriptionCommand;
import com.github.swim_developer.validator.consumer.domain.model.MessagingConstants;
import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.domain.port.in.EventGeneratorPort;
import com.github.swim_developer.validator.consumer.domain.port.in.ManageSubscriptionPort;
import com.github.swim_developer.validator.consumer.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.core.domain.model.QualityOfService;
import com.github.swim_developer.validator.core.domain.model.SubscriptionResponse;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@ApplicationScoped
public class SubscriptionService implements ManageSubscriptionPort {

    private static final String HEARTBEAT_QUEUE_SUFFIX = MessagingConstants.HEARTBEAT_QUEUE_SUFFIX;

    private final String defaultTopic;
    private final String queuePrefix;
    private final String providerName;
    private final SubscriptionRepository repository;
    private final EventGeneratorPort eventGeneratorService;

    @Inject
    public SubscriptionService(
            @ConfigProperty(name = "validator.service.default-topic", defaultValue = "SwimService") String defaultTopic,
            @ConfigProperty(name = "validator.queue.prefix", defaultValue = "SWIM") String queuePrefix,
            @ConfigProperty(name = "swim.provider.name", defaultValue = "SWIM-Validator") String providerName,
            SubscriptionRepository repository,
            EventGeneratorPort eventGeneratorService) {
        this.defaultTopic = defaultTopic;
        this.queuePrefix = queuePrefix;
        this.providerName = providerName;
        this.repository = repository;
        this.eventGeneratorService = eventGeneratorService;
    }

    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionCommand command) {
        Optional<SubscriptionEntity> existing = findExistingSubscription(command);
        if (existing.isPresent()) {
            SubscriptionEntity found = existing.get();
            log.info("Duplicate subscription detected, returning existing: id={}, queue={}, status={}",
                    found.getSubscriptionId(), found.getQueue(), found.getSubscriptionStatus());
            return toResponse(found);
        }

        log.info("Creating subscription for topic: {}", command.topic());

        String resolvedQueue = command.queueName() != null ? command.queueName() : generateQueueName(null);
        QualityOfService resolvedQos = command.qos() != null ? command.qos() : QualityOfService.AT_LEAST_ONCE;
        boolean resolvedDurable = Objects.requireNonNullElse(command.durable(), Boolean.TRUE);

        SubscriptionEntity entity = SubscriptionEntity.create(
            command.topic(), resolvedQueue, SubscriptionStatus.PAUSED, resolvedQos, resolvedDurable
        );
        entity.setSubscriptionEnd(Instant.now().plus(30, ChronoUnit.DAYS));
        entity.setProviderName(providerName);
        entity.setHeartbeatQueue(resolvedQueue + HEARTBEAT_QUEUE_SUFFIX);
        entity.setEventScenario(command.eventScenario());
        entity.setAirportHeliport(command.airportHeliport());
        entity.setAirspace(command.airspace());
        entity.setEventSeries(command.eventSeries());
        entity.setPublisher(command.publisher());
        entity.setDescription(command.description());
        entity.setComment(command.comment());

        SubscriptionEntity saved = repository.save(entity);
        eventGeneratorService.createQueueIfConnected(saved.getQueue());

        log.info("Subscription created: id={}, queue={}, status={}",
                saved.getSubscriptionId(), saved.getQueue(), saved.getSubscriptionStatus());

        return toResponse(saved);
    }

    private Optional<SubscriptionEntity> findExistingSubscription(CreateSubscriptionCommand command) {
        List<String> incomingScenarios = sorted(command.eventScenario());
        List<String> incomingAirports = sorted(command.airportHeliport());
        List<String> incomingAirspaces = sorted(command.airspace());

        return repository.findAllSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus() != SubscriptionStatus.DELETED)
                .filter(s -> sorted(s.getEventScenario()).equals(incomingScenarios))
                .filter(s -> sorted(s.getAirportHeliport()).equals(incomingAirports))
                .filter(s -> sorted(s.getAirspace()).equals(incomingAirspaces))
                .findFirst();
    }

    private List<String> sorted(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy;
    }

    @Transactional
    public SubscriptionResponse updateSubscriptionStatus(String subscriptionId, SubscriptionStatus newStatus) {
        log.info("Updating subscription status: id={}, newStatus={}", subscriptionId, newStatus);

        UUID uuid = UUID.fromString(subscriptionId);
        Optional<SubscriptionEntity> existing = repository.findBySubscriptionId(uuid);

        if (existing.isEmpty()) {
            log.warn("Subscription not found: {}, creating new one", subscriptionId);
            SubscriptionEntity entity = SubscriptionEntity.create(
                defaultTopic, generateQueueName(null), newStatus,
                QualityOfService.AT_LEAST_ONCE, true
            );
            entity.setSubscriptionId(uuid);
            String generatedQueue = entity.getQueue();
            entity.setSubscriptionEnd(Instant.now().plus(30, ChronoUnit.DAYS));
            entity.setProviderName(providerName);
            entity.setHeartbeatQueue(generatedQueue + HEARTBEAT_QUEUE_SUFFIX);
            SubscriptionEntity saved = repository.save(entity);
            return toResponse(saved);
        }

        SubscriptionEntity entity = existing.get();
        entity.setSubscriptionStatus(newStatus);
        repository.save(entity);
        return toResponse(entity);
    }

    public Optional<SubscriptionResponse> getSubscriptionDetails(String subscriptionId) {
        log.debug("Getting subscription details: {}", subscriptionId);
        try {
            UUID uuid = UUID.fromString(subscriptionId);
            return repository.findBySubscriptionId(uuid).map(this::toResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription ID format: {}", subscriptionId);
            return Optional.empty();
        }
    }

    public List<SubscriptionResponse> listSubscriptions(String queueName, SubscriptionStatus status) {
        log.debug("Listing subscriptions: queueName={}, status={}", queueName, status);

        List<SubscriptionEntity> entities;
        if (queueName != null && status != null) {
            entities = repository.findByQueueAndSubscriptionStatus(queueName, status);
        } else if (queueName != null) {
            entities = repository.findByQueue(queueName);
        } else if (status != null) {
            entities = repository.findBySubscriptionStatus(status);
        } else {
            entities = repository.findAllSubscriptions();
        }

        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteSubscription(String subscriptionId) {
        log.info("Deleting subscription: {}", subscriptionId);
        try {
            UUID uuid = UUID.fromString(subscriptionId);
            repository.findBySubscriptionId(uuid).ifPresent(repository::delete);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription ID format: {}", subscriptionId);
        }
    }

    @Transactional
    public Optional<SubscriptionResponse> renewSubscription(String subscriptionId) {
        log.info("Renewing subscription: {}", subscriptionId);
        UUID uuid = UUID.fromString(subscriptionId);
        Optional<SubscriptionEntity> existing = repository.findBySubscriptionId(uuid);

        if (existing.isEmpty()) {
            log.warn("Subscription not found for renewal: {}", subscriptionId);
            return Optional.empty();
        }

        SubscriptionEntity entity = existing.get();
        Instant currentEnd = entity.getSubscriptionEnd() != null
            ? entity.getSubscriptionEnd()
            : Instant.now();
        entity.setSubscriptionEnd(currentEnd.plus(30, ChronoUnit.DAYS));
        repository.save(entity);

        log.info("Subscription renewed: id={}, new subscriptionEnd={}",
                subscriptionId, entity.getSubscriptionEnd());
        return Optional.of(toResponse(entity));
    }

    public String generateQueueName(String userId) {
        if (userId == null || userId.isEmpty()) {
            userId = "client" + String.format("%02d", ThreadLocalRandom.current().nextInt(99) + 1);
        }
        return queuePrefix + "-" + userId + "-" + UUID.randomUUID();
    }

    private SubscriptionResponse toResponse(SubscriptionEntity entity) {
        String resolvedProvider = entity.getProviderName() != null && !entity.getProviderName().isBlank()
                ? entity.getProviderName()
                : providerName;
        String resolvedHeartbeat = entity.getHeartbeatQueue() != null && !entity.getHeartbeatQueue().isBlank()
                ? entity.getHeartbeatQueue()
                : entity.getQueue() + HEARTBEAT_QUEUE_SUFFIX;
        return new SubscriptionResponse(
                entity.getTopic(),
                entity.getSubscriptionId(),
                entity.getQueue(),
                entity.getSubscriptionStatus(),
                entity.getQos(),
                entity.getDurable(),
                entity.getSubscriptionEnd(),
                resolvedProvider,
                resolvedHeartbeat,
                entity.getEventScenario(),
                entity.getAirportHeliport(),
                entity.getAirspace(),
                entity.getEventSeries(),
                entity.getPublisher(),
                entity.getDescription(),
                entity.getComment()
        );
    }

    public List<SubscriptionEntity> listAll() {
        return repository.findAllSubscriptions();
    }

    public long countAll() {
        return repository.count();
    }

    public long countActive() {
        return repository.countBySubscriptionStatus(SubscriptionStatus.ACTIVE);
    }
}
