package com.github.swim_developer.validator.provider.domain.model;



public record ConnectionResult(boolean succeeded, String errorMessage) {

    public static ConnectionResult ok() {
        return new ConnectionResult(true, null);
    }

    public static ConnectionResult fail(String error) {
        return new ConnectionResult(false, error);
    }
}
