package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import com.github.swim_developer.validator.consumer.domain.port.in.LoadTestPort;
import com.github.swim_developer.validator.consumer.domain.port.in.XmlFileCachePort;

@Slf4j
@ApplicationScoped
public class LoadTestService implements LoadTestPort {

    private final XmlFileCachePort xmlEventFileLoader;
    private final AmqpConnectionManager amqpConnectionManager;
    private final SubscriptionRepository subscriptionRepository;

    @Inject
    public LoadTestService(
            XmlFileCachePort xmlEventFileLoader,
            AmqpConnectionManager amqpConnectionManager,
            SubscriptionRepository subscriptionRepository) {
        this.xmlEventFileLoader = xmlEventFileLoader;
        this.amqpConnectionManager = amqpConnectionManager;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Multi<String> executeLoad(String durationStr) {
        Duration duration = parseDuration(durationStr);
        long endTime = System.currentTimeMillis() + duration.toMillis();

        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        List<SubscriptionEntity> activeSubs = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int batchSize = availableProcessors * 10;

        return Multi.createFrom().ticks().every(Duration.ofMillis(100))
                .onItem().transform(tick -> {
                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        IntStream.range(0, batchSize).forEach(i ->
                                executor.submit(() -> {
                                    try {
                                        sendCachedEvent(xmlCache, activeSubs);
                                        sentCount.incrementAndGet();
                                    } catch (Exception e) {
                                        errorCount.incrementAndGet();
                                    }
                                })
                        );
                    }
                    long remaining = (endTime - System.currentTimeMillis()) / 1000;
                    return String.format("CPUs: %d | Remaining: %ds | Sent: %d | Errors: %d",
                            availableProcessors, Math.max(0, remaining), sentCount.get(), errorCount.get());
                })
                .select().first(item -> System.currentTimeMillis() < endTime)
                .onTermination().invoke(xmlEventFileLoader::clearXmlCache);
    }

    private Duration parseDuration(String durationStr) {
        String unit = durationStr.substring(durationStr.length() - 1);
        long value = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
        return switch (unit) {
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            default -> throw new IllegalArgumentException("Invalid Time Unit: " + unit);
        };
    }

    private void sendCachedEvent(List<String> cache, List<SubscriptionEntity> subscriptions) {
        if (cache.isEmpty() || subscriptions.isEmpty()) return;
        String xmlContent = cache.get(ThreadLocalRandom.current().nextInt(cache.size()));
        for (SubscriptionEntity sub : subscriptions) {
            amqpConnectionManager.sendToQueue(sub.getQueue(), xmlContent, "cached-event.xml");
        }
    }
}
