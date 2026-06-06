package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@ApplicationScoped
public class AmqpConnectionManager {

    public static final String AMQP_CONNECTED_ADDRESS = "amqp.connected";

    private final boolean brokerEnabled;
    private final String brokerHost;
    private final int brokerPort;
    private final String brokerUsername;
    private final String brokerPassword;
    private final Vertx vertx;

    private final AtomicReference<AmqpClient> amqpClient = new AtomicReference<>();
    private final AtomicReference<AmqpConnection> amqpConnection = new AtomicReference<>();
    private volatile boolean connectionAttempted = false;
    private volatile String lastConnectionError = null;
    private volatile boolean reconnecting = false;

    @Inject
    public AmqpConnectionManager(
            @ConfigProperty(name = "amqp.broker.enabled", defaultValue = "true") boolean brokerEnabled,
            @ConfigProperty(name = "amqp.broker.host", defaultValue = "localhost") String brokerHost,
            @ConfigProperty(name = "amqp.broker.port", defaultValue = "5672") int brokerPort,
            @ConfigProperty(name = "amqp.broker.username", defaultValue = "admin") String brokerUsername,
            @ConfigProperty(name = "amqp.broker.password", defaultValue = "admin") String brokerPassword,
            Vertx vertx) {
        this.brokerEnabled = brokerEnabled;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.vertx = vertx;
    }

    @PostConstruct
    void init() {
        if (!brokerEnabled) {
            log.info("AMQP broker connection is disabled");
            return;
        }
        connect();
    }

    public void connect() {
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
        connectionAttempted = true;

        client.connect().onSuccess(conn -> {
            amqpConnection.set(conn);
            lastConnectionError = null;
            reconnecting = false;
            log.info("Connected to AMQP broker at {}:{}", brokerHost, brokerPort);
            vertx.eventBus().publish(AMQP_CONNECTED_ADDRESS, brokerHost + ":" + brokerPort);

            conn.exceptionHandler(err -> {
                log.error("AMQP connection error", err);
                lastConnectionError = err.getMessage();
                amqpConnection.set(null);
                reconnecting = false;
            });
        }).onFailure(err -> {
            log.error("Failed to connect to AMQP broker at {}:{}", brokerHost, brokerPort, err);
            lastConnectionError = err.getMessage();
            amqpConnection.set(null);
            reconnecting = false;
        });
    }

    public boolean isConnected() {
        if (!brokerEnabled) {
            return false;
        }
        AmqpConnection conn = amqpConnection.get();
        return conn != null && !conn.isDisconnected();
    }

    public boolean isDisconnected() {
        return !isConnected();
    }

    public void createQueueIfConnected(String queueName) {
        log.info("createQueueIfConnected called for queue: {}", queueName);
        if (!isConnected()) {
            log.warn("Cannot create queue {}: AMQP not connected", queueName);
            return;
        }
        materializeQueue(queueName);
        materializeQueue(queueName + HeartbeatPublisher.HEARTBEAT_QUEUE_SUFFIX);
    }

    private void materializeQueue(String queueName) {
        AmqpConnection conn = amqpConnection.get();
        if (conn == null) {
            return;
        }
        conn.createReceiver(queueName).onSuccess(receiver -> {
            log.info("Queue created via receiver: {}", queueName);
            receiver.close();
        }).onFailure(err -> log.error("Failed to create receiver for queue {}: {}", queueName, err.getMessage()));
    }

    public void sendToQueue(String queueName, String xmlContent, String filename) {
        if (isDisconnected()) {
            throw new IllegalStateException("AMQP connection not available");
        }
        AmqpConnection conn = amqpConnection.get();
        if (conn == null) {
            throw new IllegalStateException("AMQP connection not available");
        }
        conn.createSender(queueName).onSuccess(sender -> {
            String messageId = "ID:" + UUID.randomUUID();
            AmqpMessage message = AmqpMessage.create()
                    .id(messageId)
                    .withBody(xmlContent)
                    .contentType("application/xml")
                    .durable(true)
                    .build();

            sender.sendWithAck(message).onSuccess(ack -> {
                log.debug("Message sent - Queue: {}, MessageId: {}, Event: {}", queueName, messageId, filename);
                sender.close();
            }).onFailure(err -> {
                log.error("Failed to send message - Queue: {}, MessageId: {}, Event: {}, Error: {}",
                        queueName, messageId, filename, err.getMessage());
                sender.close();
            });
        }).onFailure(err -> log.error("Failed to create sender for queue {}", queueName, err));
    }

    public String getBrokerInfo() {
        if (!brokerEnabled) return "disabled";
        if (!connectionAttempted) return "not-attempted";
        if (isConnected()) return String.format("%s:%d (connected)", brokerHost, brokerPort);
        return String.format("%s:%d (disconnected: %s)", brokerHost, brokerPort,
                lastConnectionError != null ? lastConnectionError : "unknown");
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
