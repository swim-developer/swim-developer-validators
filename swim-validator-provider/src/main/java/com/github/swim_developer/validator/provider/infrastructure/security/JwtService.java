package com.github.swim_developer.validator.provider.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.swim_developer.validator.provider.infrastructure.rest.dto.UserInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@ApplicationScoped
public class JwtService {

    private static final Pattern JWT_PART_SEPARATOR = Pattern.compile("\\.");

    private final ObjectMapper objectMapper;

    @Inject
    public JwtService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<UserInfo> extractUserInfo(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authHeader.substring(7);
        return parseToken(token);
    }

    public Optional<UserInfo> parseToken(String token) {
        try {
            String[] parts = JWT_PART_SEPARATOR.split(token, 0);
            if (parts.length != 3) {
                log.warn("Invalid JWT format");
                return Optional.empty();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode claims = objectMapper.readTree(payload);

            long exp = claims.has("exp") ? claims.get("exp").asLong() : 0;
            if (exp > 0 && Instant.now().getEpochSecond() > exp) {
                log.warn("JWT token expired");
                return Optional.empty();
            }

            String userId = claims.has("sub") ? claims.get("sub").asText() : null;
            String username;
            if (claims.has("preferred_username")) {
                username = claims.get("preferred_username").asText();
            } else if (claims.has("email")) {
                username = claims.get("email").asText();
            } else {
                username = userId;
            }

            if (userId == null) {
                log.warn("JWT missing 'sub' claim");
                return Optional.empty();
            }

            return Optional.of(new UserInfo(userId, username, token));
        } catch (Exception e) {
            log.error("Failed to parse JWT", e);
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        return parseToken(token).isPresent();
    }

    public Optional<UserInfo> extractUserFromHeader(String authHeader) {
        return extractUserInfo(authHeader);
    }
}
