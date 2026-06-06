package com.github.swim_developer.validator.core.infrastructure.rest.dto;

import com.github.swim_developer.validator.core.domain.model.TopicSummary;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "List of topics")
public record TopicList(
    @Schema(description = "Array of available topics")
    List<TopicSummary> topics
) {}
