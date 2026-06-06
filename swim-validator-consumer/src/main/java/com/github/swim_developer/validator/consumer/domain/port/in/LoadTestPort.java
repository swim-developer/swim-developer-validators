package com.github.swim_developer.validator.consumer.domain.port.in;

import io.smallrye.mutiny.Multi;

public interface LoadTestPort {
    Multi<String> executeLoad(String durationStr);
}
