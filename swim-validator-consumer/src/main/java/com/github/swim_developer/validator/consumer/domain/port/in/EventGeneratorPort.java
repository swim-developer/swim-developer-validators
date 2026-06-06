package com.github.swim_developer.validator.consumer.domain.port.in;

import java.util.Optional;

public interface EventGeneratorPort {
    void createQueueIfConnected(String queueName);
    String getBrokerInfo();
    boolean isAmqpConnected();
    boolean isSchedulerEnabled();
    boolean toggleScheduler();
    Optional<String> generateAndSendEventManually();
    int sendScenarioEvent(String xmlContent, String scenarioName);
    void sendDuplicateScenarioEvent(String xmlContent);
    int sendSpecificEvent(String filename);
    void sendToQueue(String queueName, String xmlContent, String filename);
    void generateAndSendEvent();
}
