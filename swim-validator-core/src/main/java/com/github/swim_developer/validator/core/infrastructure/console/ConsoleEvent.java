package com.github.swim_developer.validator.core.infrastructure.console;

public record ConsoleEvent(String type, String message) {

    public String toJson() {
        return "{\"type\":\"" + type + "\",\"message\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
