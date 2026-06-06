package com.github.swim_developer.validator.consumer.infrastructure.persistence;

import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.consumer.infrastructure.persistence.entity.SubscriptionJpaEntity;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SubscriptionRepositoryImpl implements SubscriptionRepository, PanacheRepositoryBase<SubscriptionJpaEntity, UUID> {

    private final ConsumerSubscriptionMapper mapper;

    @Inject
    public SubscriptionRepositoryImpl(ConsumerSubscriptionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<SubscriptionEntity> findBySubscriptionId(UUID subscriptionId) {
        return find("subscriptionId", subscriptionId).firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<SubscriptionEntity> findByQueue(String queue) {
        return list("queue", queue).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SubscriptionEntity> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        return list("subscriptionStatus", subscriptionStatus).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SubscriptionEntity> findByQueueAndSubscriptionStatus(String queue, SubscriptionStatus subscriptionStatus) {
        return list("queue = ?1 and subscriptionStatus = ?2", queue, subscriptionStatus)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countBySubscriptionStatus(SubscriptionStatus status) {
        return count("subscriptionStatus", status);
    }

    @Override
    public SubscriptionEntity save(SubscriptionEntity domain) {
        SubscriptionJpaEntity jpa = mapper.toJpa(domain);
        var em = getEntityManager();
        if (!em.contains(jpa) && findById(jpa.getSubscriptionId()) == null) {
            persist(jpa);
        } else {
            jpa = em.merge(jpa);
        }
        return mapper.toDomain(jpa);
    }

    @Override
    public void delete(SubscriptionEntity domain) {
        SubscriptionJpaEntity jpa = mapper.toJpa(domain);
        delete(jpa);
    }

    @Override
    public long deleteAll() {
        return getEntityManager()
                .createQuery("DELETE FROM SubscriptionJpaEntity")
                .executeUpdate();
    }

    @Override
    public List<SubscriptionEntity> findAllSubscriptions() {
        return listAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count() {
        return getEntityManager()
                .createQuery("SELECT COUNT(s) FROM SubscriptionJpaEntity s", Long.class)
                .getSingleResult();
    }
}
