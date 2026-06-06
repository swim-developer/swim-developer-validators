package com.github.swim_developer.validator.core.infrastructure.rest.dto;

import com.github.swim_developer.validator.core.domain.model.SubscriptionResponse;

import java.util.List;

public record SubscriptionList(List<SubscriptionResponse> subscriptions) {}
