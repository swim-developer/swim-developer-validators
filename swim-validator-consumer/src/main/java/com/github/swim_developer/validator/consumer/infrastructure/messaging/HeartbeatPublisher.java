package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import com.github.swim_developer.validator.consumer.domain.model.MessagingConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.swim_developer.validator.consumer.domain.model.SubscriptionEntity;
import com.github.swim_developer.validator.consumer.domain.port.out.SubscriptionRepository;
import com.github.swim_developer.validator.core.domain.model.SubscriptionStatus;
import io.quarkus.scheduler.Scheduled;
import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.github.swim_developer.validator.consumer.domain.port.in.HeartbeatPort;

@Slf4j
@ApplicationScoped
public class HeartbeatPublisher implements HeartbeatPort {

    public static final String HEARTBEAT_QUEUE_SUFFIX = MessagingConstants.HEARTBEAT_QUEUE_SUFFIX;

    private final boolean enabled;
    private final String intervalStr;
    private final String brokerHost;
    private final int brokerPort;
    private final String brokerUsername;
    private final String brokerPassword;
    private final Vertx vertx;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    private final AtomicReference<AmqpClient> amqpClient = new AtomicReference<>();
    private final AtomicReference<AmqpConnection> amqpConnection = new AtomicReference<>();
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    private volatile boolean running = true;
    private volatile boolean reconnecting = false;

    @Inject
    public HeartbeatPublisher(
            @ConfigProperty(name = "heartbeat.publisher.enabled", defaultValue = "true") boolean enabled,
            @ConfigProperty(name = "heartbeat.publisher.interval", defaultValue = "15s") String intervalStr,
            @ConfigProperty(name = "amqp.broker.host", defaultValue = "localhost") String brokerHost,
            @ConfigProperty(name = "amqp.broker.port", defaultValue = "5672") int brokerPort,
            @ConfigProperty(name = "amqp.broker.username", defaultValue = "admin") String brokerUsername,
            @ConfigProperty(name = "amqp.broker.password", defaultValue = "admin") String brokerPassword,
            Vertx vertx,
            SubscriptionRepository subscriptionRepository,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.intervalStr = intervalStr;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.vertx = vertx;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        if (!enabled) {
            log.info("Heartbeat publisher disabled");
            return;
        }
        connectToBroker();
    }

    public void stop() {
        running = false;
        log.warn("Heartbeat publisher STOPPED");
    }

    public void start() {
        running = true;
        log.info("Heartbeat publisher STARTED");
    }

    public boolean isRunning() {
        return running;
    }

    private boolean isDisconnected() {
        AmqpConnection conn = amqpConnection.get();
        return conn == null || conn.isDisconnected();
    }

    private String brokerUrlForLog() {
        return "amqp://" + brokerHost + ":" + brokerPort;
    }

    private void connectToBroker() {
        if (reconnecting) {
            return;
        }
        reconnecting = true;

        AmqpClient previous = amqpClient.getAndSet(null);
        if (previous != null) {
            try {
                previous.close();
            } catch (Exception e) {
                log.trace("AMQP client close", e);
            }
        }
        amqpConnection.set(null);

        AmqpClientOptions options = new AmqpClientOptions()
                .setHost(brokerHost)
                .setPort(brokerPort)
                .setUsername(brokerUsername)
                .setPassword(brokerPassword)
                .setReconnectAttempts(10)
                .setReconnectInterval(5000);

        AmqpClient client = AmqpClient.create(vertx, options);
        amqpClient.set(client);

        client.connect()
                .onSuccess(conn -> {
                    amqpConnection.set(conn);
                    reconnecting = false;
                    log.info("Heartbeat publisher connected to AMQP broker at {}:{}", brokerHost, brokerPort);
                    conn.exceptionHandler(err -> {
                        log.error("Heartbeat publisher AMQP connection error", err);
                        amqpConnection.set(null);
                        reconnecting = false;
                    });
                })
                .onFailure(err -> {
                    log.warn("Heartbeat publisher failed to connect: {}", err.getMessage());
                    amqpConnection.set(null);
                    reconnecting = false;
                });
    }

    @Scheduled(every = "${heartbeat.publisher.interval:15s}")
    @Transactional
    public void publishHeartbeat() {
        try {
            if (!enabled || !running) {
                return;
            }

            if (isDisconnected()) {
                log.warn("Heartbeat publisher: AMQP not connected, triggering reconnection");
                connectToBroker();
                return;
            }

            List<SubscriptionEntity> active = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
            List<SubscriptionEntity> paused = subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.PAUSED);
            List<SubscriptionEntity> all = new ArrayList<>(active);
            all.addAll(paused);

            if (all.isEmpty()) {
                log.info("No active/paused subscriptions, skipping heartbeat cycle");
                return;
            }

            long seq = sequenceCounter.incrementAndGet();
            Instant now = Instant.now();
            Duration interval = parseInterval(intervalStr);
            Instant next = now.plus(interval);

            log.info("Heartbeat cycle seq={} for {} subscription(s)", seq, all.size());

            for (SubscriptionEntity sub : all) {
                String heartbeatQueue = sub.getQueue() + HEARTBEAT_QUEUE_SUFFIX;
                String payload = buildHeartbeatJson(sub, seq, now, next);
                sendToHeartbeatQueue(heartbeatQueue, payload, seq);
            }
        } catch (Exception e) {
            log.error("Heartbeat publisher cycle failed", e);
        }
    }

    private void sendToHeartbeatQueue(String queueName, String payload, long seq) {
        log.info("Dispatching heartbeat send - queue={}, seq={}", queueName, seq);
        vertx.runOnContext(v -> {
            try {
                AmqpConnection conn = amqpConnection.get();
                if (conn == null) {
                    return;
                }
                conn.createSender(queueName)
                        .onSuccess(sender -> {
                            AmqpMessage message = AmqpMessage.create()
                                    .withBody(payload)
                                    .contentType("application/json")
                                    .build();
                            sender.sendWithAck(message)
                                    .onSuccess(ack -> {
                                        log.info("Heartbeat ACKed - brokerUrl={}, queue={}, seq={}",
                                                brokerUrlForLog(), queueName, seq);
                                        sender.close();
                                    })
                                    .onFailure(err -> {
                                        log.warn("Heartbeat send failed - brokerUrl={}, queue={}, seq={}, cause={}",
                                                brokerUrlForLog(), queueName, seq, err.getMessage());
                                        sender.close();
                                    });
                        })
                        .onFailure(err -> log.warn("Failed to create heartbeat sender - brokerUrl={}, queue={}, cause={}",
                                brokerUrlForLog(), queueName, err.getMessage()));
            } catch (Exception e) {
                log.error("Exception in heartbeat send - queue={}, seq={}", queueName, seq, e);
            }
        });
    }

    private String buildHeartbeatJson(SubscriptionEntity sub, long seq, Instant now, Instant next) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("subscriptionId", sub.getSubscriptionId().toString());
            payload.put("subscriptionState", sub.getSubscriptionStatus().name());
            payload.put("providerStatus", "OPERATIONAL");
            payload.put("publicationTime", now.toString());
            payload.put("nextPublicationTime", next.toString());
            payload.put("sequenceNumber", seq);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to build heartbeat JSON for subscription {}", sub.getSubscriptionId(), e);
            return "{}";
        }
    }

    private Duration parseInterval(String value) {
        if (value == null || value.isBlank()) {
            return Duration.ofSeconds(15);
        }
        String trimmed = value.trim().toLowerCase();
        if (trimmed.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        return Duration.ofSeconds(15);
    }

    @PreDestroy
    void cleanup() {
        AmqpConnection conn = amqpConnection.getAndSet(null);
        if (conn != null) {
            conn.close();
        }
        AmqpClient client = amqpClient.getAndSet(null);
        if (client != null) {
            client.close();
        }
    }
}
