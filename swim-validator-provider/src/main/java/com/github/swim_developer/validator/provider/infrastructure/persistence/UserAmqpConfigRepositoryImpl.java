package com.github.swim_developer.validator.provider.infrastructure.persistence;

import com.github.swim_developer.validator.provider.domain.model.UserAmqpConfig;
import com.github.swim_developer.validator.provider.domain.port.out.UserAmqpConfigRepository;
import com.github.swim_developer.validator.provider.infrastructure.persistence.entity.UserAmqpConfigEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class UserAmqpConfigRepositoryImpl implements UserAmqpConfigRepository, PanacheRepositoryBase<UserAmqpConfigEntity, Long> {

    private final ProviderValidatorMapper mapper;

    @Inject
    public UserAmqpConfigRepositoryImpl(ProviderValidatorMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserAmqpConfig insert(UserAmqpConfig domain) {
        UserAmqpConfigEntity entity = mapper.toEntity(domain);
        persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public UserAmqpConfig update(UserAmqpConfig domain) {
        UserAmqpConfigEntity entity = mapper.toEntity(domain);
        UserAmqpConfigEntity merged = getEntityManager().merge(entity);
        return mapper.toDomain(merged);
    }

    @Override
    public Optional<UserAmqpConfig> findByUserId(String userId) {
        return find("userId", userId).firstResultOptional()
                .map(mapper::toDomain);
    }
}
