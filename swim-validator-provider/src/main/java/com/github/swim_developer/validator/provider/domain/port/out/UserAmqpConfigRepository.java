package com.github.swim_developer.validator.provider.domain.port.out;

import com.github.swim_developer.validator.provider.domain.model.UserAmqpConfig;

import java.util.Optional;

public interface UserAmqpConfigRepository {
    UserAmqpConfig insert(UserAmqpConfig entity);
    UserAmqpConfig update(UserAmqpConfig entity);
    Optional<UserAmqpConfig> findByUserId(String userId);
}
