package com.github.swim_developer.validator.consumer.domain.model;

public record ScenarioPreview(String xml, String breakInfo) {
    public static final String BREAK_MARKER = "<!-- \u26d4 INTENTIONAL BREAK: Tag removed here -->";
}
