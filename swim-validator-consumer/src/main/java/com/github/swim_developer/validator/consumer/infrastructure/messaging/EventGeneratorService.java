package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import com.github.swim_developer.validator.consumer.domain.port.in.EventGeneratorPort;
import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import com.github.swim_developer.validator.core.infrastructure.console.ConsoleNotificationService;
import com.github.swim_developer.validator.core.infrastructure.util.XmlDateRandomizer;
import com.github.swim_developer.validator.core.infrastructure.util.XmlEd254DateRandomizer;
import com.github.swim_developer.validator.core.infrastructure.util.XmlLocationRandomizer;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.swim_developer.validator.consumer.domain.port.in.XmlFileCachePort;

@Slf4j
@ApplicationScoped
@io.quarkus.runtime.Startup
public class EventGeneratorService implements EventGeneratorPort {

    private final boolean schedulerEnabledConfig;
    private final SubscriptionRepository subscriptionRepository;
    private final AmqpConnectionManager amqpConnectionManager;
    private final Vertx vertx;
    private final ConsoleNotificationService consoleNotificationService;
    private final XmlDateRandomizer xmlDateRandomizer;
    private final XmlEd254DateRandomizer xmlEd254DateRandomizer;
    private final XmlLocationRandomizer xmlLocationRandomizer;
    private final XmlFileCachePort xmlEventFileLoader;

    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);

    @Inject
    public EventGeneratorService(
            @ConfigProperty(name = "event.generator.enabled", defaultValue = "true") boolean schedulerEnabledConfig,
            SubscriptionRepository subscriptionRepository,
            AmqpConnectionManager amqpConnectionManager,
            Vertx vertx,
            ConsoleNotificationService consoleNotificationService,
            XmlDateRandomizer xmlDateRandomizer,
            XmlEd254DateRandomizer xmlEd254DateRandomizer,
            XmlLocationRandomizer xmlLocationRandomizer,
            XmlFileCachePort xmlEventFileLoader) {
        this.schedulerEnabledConfig = schedulerEnabledConfig;
        this.subscriptionRepository = subscriptionRepository;
        this.amqpConnectionManager = amqpConnectionManager;
        this.vertx = vertx;
        this.consoleNotificationService = consoleNotificationService;
        this.xmlDateRandomizer = xmlDateRandomizer;
        this.xmlEd254DateRandomizer = xmlEd254DateRandomizer;
        this.xmlLocationRandomizer = xmlLocationRandomizer;
        this.xmlEventFileLoader = xmlEventFileLoader;
    }

    @PostConstruct
    void init() {
        schedulerEnabled.set(schedulerEnabledConfig);
        log.info("Scheduler initialized with enabled={}", schedulerEnabled.get());
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled.get();
    }

    public void setSchedulerEnabled(boolean enabled) {
        schedulerEnabled.set(enabled);
        log.info("Scheduler enabled changed to: {}", enabled);
    }

    public boolean toggleScheduler() {
        boolean v = !schedulerEnabled.get();
        schedulerEnabled.set(v);
        log.info("Scheduler toggled to: {}", v);
        return v;
    }

    public boolean isAmqpConnected() {
        return amqpConnectionManager.isConnected();
    }

    public void createQueueIfConnected(String queueName) {
        amqpConnectionManager.createQueueIfConnected(queueName);
    }

    public String getBrokerInfo() {
        return amqpConnectionManager.getBrokerInfo();
    }

    @Scheduled(cron = "{event.generator.schedule}")
    public void generateAndSendEvent() {
        log.info("Event generator cycle started");

        if (!schedulerEnabled.get()) {
            log.info("Event generator scheduler is disabled, cycle finished");
            return;
        }

        consoleNotificationService.info("⏰ Scheduler triggered");

        if (amqpConnectionManager.isDisconnected()) {
            log.warn("AMQP connection not available, triggering reconnection");
            consoleNotificationService.warning("⏰ Scheduler: AMQP not connected, reconnecting...");
            amqpConnectionManager.connect();
            return;
        }

        try {
            List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);

            if (activeSubscriptions.isEmpty()) {
                log.info("No active subscriptions, skipping event generation, cycle finished");
                consoleNotificationService.info("⏰ Scheduler: No active subscriptions");
                return;
            }

            List<Path> xmlFiles = xmlEventFileLoader.listXmlFiles();
            if (xmlFiles.isEmpty()) {
                log.warn("No XML files found, cycle finished");
                consoleNotificationService.warning("⏰ Scheduler: No event files found");
                return;
            }

            log.info("Sending random events to {} active subscription(s)", activeSubscriptions.size());

            for (SubscriptionEntity subscription : activeSubscriptions) {
                Path selectedFile = xmlEventFileLoader.selectRandomFile(xmlFiles);
                String xmlContent = Files.readString(selectedFile);
                String filename = selectedFile.getFileName().toString();
                boolean applyRandomDates = ThreadLocalRandom.current().nextBoolean();
                if (applyRandomDates) {
                    xmlContent = xmlDateRandomizer.randomizeDates(xmlContent);
                    xmlContent = xmlEd254DateRandomizer.randomizeDates(xmlContent);
                }
                xmlContent = xmlLocationRandomizer.randomizeLocation(xmlContent);
                log.info("Event selected - File: {}, Queue: {}, RandomizeDates: {}",
                        filename, subscription.getQueue(), applyRandomDates);
                amqpConnectionManager.sendToQueue(subscription.getQueue(), xmlContent, filename);
            }

            consoleNotificationService.success("⏰ Scheduler: Sent events to " + activeSubscriptions.size() + " subscription(s)");
            log.info("Random events sent to all active subscriptions, cycle finished successfully");

        } catch (Exception e) {
            log.error("Failed to generate and send event, cycle finished with error", e);
            consoleNotificationService.error("⏰ Scheduler error: " + e.getMessage());
        }
    }

    public void sendToQueue(String queueName, String xmlContent, String filename) {
        amqpConnectionManager.sendToQueue(queueName, xmlContent, filename);
    }

    public Optional<String> generateAndSendEventManually() {
        if (amqpConnectionManager.isDisconnected()) {
            throw new IllegalStateException("AMQP connection not available");
        }

        List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        if (activeSubscriptions.isEmpty()) {
            log.debug("No active subscriptions found");
            return Optional.empty();
        }

        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        if (xmlCache.isEmpty()) {
            throw new IllegalStateException("No XML events found in cache or directory");
        }

        String lastXmlContent = null;
        for (SubscriptionEntity subscription : activeSubscriptions) {
            String xmlContent = xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
            amqpConnectionManager.sendToQueue(subscription.getQueue(), xmlContent, "event-from-cache.xml");
            lastXmlContent = xmlContent;
        }
        return Optional.ofNullable(lastXmlContent);
    }

    public int sendScenarioEvent(String xmlContent, String scenarioName) {
        if (amqpConnectionManager.isDisconnected()) {
            throw new IllegalStateException("AMQP connection not available");
        }

        List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        if (activeSubscriptions.isEmpty()) {
            return 0;
        }

        for (SubscriptionEntity subscription : activeSubscriptions) {
            amqpConnectionManager.sendToQueue(subscription.getQueue(), xmlContent, scenarioName + "-scenario.xml");
        }

        log.info("Sent {} scenario to {} subscription(s)", scenarioName, activeSubscriptions.size());
        return activeSubscriptions.size();
    }

    public void sendDuplicateScenarioEvent(String xmlContent) {
        if (amqpConnectionManager.isDisconnected()) {
            throw new IllegalStateException("AMQP connection not available");
        }

        List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        if (activeSubscriptions.isEmpty()) {
            throw new IllegalStateException("No active subscriptions");
        }

        for (SubscriptionEntity subscription : activeSubscriptions) {
            amqpConnectionManager.sendToQueue(subscription.getQueue(), xmlContent, "duplicate-scenario.xml");
        }
        log.info("Sent first duplicate message to {} subscription(s)", activeSubscriptions.size());

        vertx.setTimer(10000, id -> {
            for (SubscriptionEntity subscription : activeSubscriptions) {
                amqpConnectionManager.sendToQueue(subscription.getQueue(), xmlContent, "duplicate-scenario.xml");
            }
            log.info("Sent second duplicate message to {} subscription(s)", activeSubscriptions.size());
        });
    }

    public int sendSpecificEvent(String filename) {
        return sendScenarioEvent(xmlEventFileLoader.readEventFile(filename), filename);
    }
}
