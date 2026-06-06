package com.github.swim_developer.validator.consumer.domain.port.in;

public interface HeartbeatPort {
    boolean isRunning();
    void start();
    void stop();
}
