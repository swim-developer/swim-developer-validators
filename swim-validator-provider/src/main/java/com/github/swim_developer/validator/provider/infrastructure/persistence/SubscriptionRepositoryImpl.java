package com.github.swim_developer.validator.provider.infrastructure.persistence;

import com.github.swim_developer.validator.provider.domain.model.Subscription;
import com.github.swim_developer.validator.provider.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.provider.infrastructure.persistence.entity.SubscriptionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SubscriptionRepositoryImpl implements SubscriptionRepository, PanacheRepositoryBase<SubscriptionEntity, Long> {

    private final ProviderValidatorMapper mapper;

    @Inject
    public SubscriptionRepositoryImpl(ProviderValidatorMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Subscription insert(Subscription domain) {
        SubscriptionEntity entity = mapper.toEntity(domain);
        persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Subscription update(Subscription domain) {
        SubscriptionEntity entity = mapper.toEntity(domain);
        SubscriptionEntity merged = getEntityManager().merge(entity);
        return mapper.toDomain(merged);
    }

    @Override
    public List<Subscription> findByUsername(String username) {
        return list("username", username).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Subscription> findBySubscriptionId(String subscriptionId) {
        return find("subscriptionId", subscriptionId).firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public long countByUsernameAndStatusNot(String username, String status) {
        return count("username = ?1 and status <> ?2", username, status);
    }
}
