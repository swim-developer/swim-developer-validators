package com.github.swim_developer.validator.core.domain.model;

import java.util.List;

public record TopicData(
        String title,
        String summaryDescription,
        String detailedDescription,
        List<String> features,
        List<String> mandatoryFor,
        List<String> useCases
) {}
