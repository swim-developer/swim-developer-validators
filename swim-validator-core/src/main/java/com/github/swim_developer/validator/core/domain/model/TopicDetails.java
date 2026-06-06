package com.github.swim_developer.validator.core.domain.model;

import java.util.List;

public record TopicDetails(
    String topicId,
    String title,
    String description,
    String eventScenario,
    List<String> features,
    List<String> mandatoryFor,
    List<String> useCase
) {}
