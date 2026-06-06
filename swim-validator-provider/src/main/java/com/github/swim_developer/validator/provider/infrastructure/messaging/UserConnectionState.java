package com.github.swim_developer.validator.provider.infrastructure.messaging;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpReceiver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
public class UserConnectionState {

    private static final long TOKEN_EXPIRY_BUFFER_SECONDS = 60;
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private static final Pattern JWT_PART_SEPARATOR = Pattern.compile("\\.");

    private final String userId;
    private final String username;
    private AmqpClient client;
    private AmqpConnection connection;
    private Instant lastHeartbeat;
    private Instant connectedAt;
    private String host;
    private int port;
    private String token;
    private Instant tokenExpiresAt;

    private final Map<String, AmqpReceiver> receivers = new ConcurrentHashMap<>();

    public UserConnectionState(String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.lastHeartbeat = Instant.now();
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = Instant.now();
    }

    public void setTokenWithExpiry(String jwtToken) {
        this.token = jwtToken;
        this.tokenExpiresAt = parseExpFromJwt(jwtToken);
    }

    public boolean isTokenExpiringSoon() {
        if (tokenExpiresAt == null) {
            return true;
        }
        return Instant.now().isAfter(tokenExpiresAt.minusSeconds(TOKEN_EXPIRY_BUFFER_SECONDS));
    }

    private static Instant parseExpFromJwt(String jwt) {
        try {
            String[] parts = JWT_PART_SEPARATOR.split(jwt, 0);
            if (parts.length < 2) {
                return Instant.now();
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Matcher matcher = EXP_PATTERN.matcher(payloadJson);
            if (matcher.find()) {
                long exp = Long.parseLong(matcher.group(1));
                return Instant.ofEpochSecond(exp);
            }
            return Instant.now();
        } catch (Exception e) {
            log.warn("Failed to parse JWT exp claim, assuming expired", e);
            return Instant.now();
        }
    }

    public boolean isExpired(long timeoutSeconds) {
        if (lastHeartbeat == null) {
            return true;
        }
        return Instant.now().isAfter(lastHeartbeat.plusSeconds(timeoutSeconds));
    }

    public int getActiveReceiverCount() {
        return receivers.size();
    }

    public void addReceiver(String queueName, AmqpReceiver receiver) {
        receivers.put(queueName, receiver);
    }

    public AmqpReceiver removeReceiver(String queueName) {
        return receivers.remove(queueName);
    }

    public boolean hasReceiver(String queueName) {
        return receivers.containsKey(queueName);
    }

    public void closeAllReceivers() {
        for (Map.Entry<String, AmqpReceiver> entry : receivers.entrySet()) {
            try {
                entry.getValue().close();
                log.debug("Closed receiver for queue: {} (user: {})", entry.getKey(), username);
            } catch (Exception e) {
                log.warn("Error closing receiver for queue: {}", entry.getKey(), e);
            }
        }
        receivers.clear();
    }

    public void disconnect() {
        closeAllReceivers();

        if (connection != null) {
            try {
                connection.close();
                log.info("Disconnected AMQP connection for user: {}", username);
            } catch (Exception e) {
                log.warn("Error closing AMQP connection for user: {}", username, e);
            }
            connection = null;
        }

        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing AMQP client for user: {}", username, e);
            }
            client = null;
        }

        connectedAt = null;
    }
}
