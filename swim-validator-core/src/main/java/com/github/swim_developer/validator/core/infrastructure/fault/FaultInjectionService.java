package com.github.swim_developer.validator.core.infrastructure.fault;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@ApplicationScoped
public class FaultInjectionService {

    private final ConcurrentHashMap<String, FaultConfig> activeFaults = new ConcurrentHashMap<>();
    private final AtomicLong faultIdCounter = new AtomicLong(0);

    public String addFault(String pathPattern, String httpMethod, Integer httpStatus,
                           Long delayMs, Double dropRate, Integer durationSeconds) {
        String faultId = "fault-" + faultIdCounter.incrementAndGet();
        Instant expiresAt = durationSeconds != null
                ? Instant.now().plus(durationSeconds, ChronoUnit.SECONDS)
                : null;

        activeFaults.put(faultId, new FaultConfig(pathPattern, httpMethod, httpStatus, delayMs, dropRate, expiresAt));
        log.warn("Fault injection added - ID: {}, Pattern: {}, Method: {}, Status: {}, Delay: {}ms, DropRate: {}%, ExpiresAt: {}",
                faultId, pathPattern, httpMethod, httpStatus, delayMs, dropRate, expiresAt);
        return faultId;
    }

    public void removeFault(String faultId) {
        FaultConfig removed = activeFaults.remove(faultId);
        if (removed != null) {
            log.info("Fault injection removed - ID: {}", faultId);
        }
    }

    public void clearAll() {
        int count = activeFaults.size();
        activeFaults.clear();
        log.info("Cleared all fault injections - {} fault(s) removed", count);
    }

    public List<FaultStatus> listActiveFaults() {
        return activeFaults.entrySet().stream()
                .map(e -> new FaultStatus(
                        e.getKey(),
                        e.getValue().pathPattern(),
                        e.getValue().httpMethod(),
                        e.getValue().httpStatus(),
                        e.getValue().delayMs(),
                        e.getValue().dropRate(),
                        e.getValue().expiresAt(),
                        e.getValue().isExpired()))
                .toList();
    }

    public Optional<FaultConfig> findMatchingFault(String method, String path) {
        activeFaults.entrySet().removeIf(e -> e.getValue().isExpired());
        return activeFaults.values().stream()
                .filter(f -> f.matches(method, path))
                .findFirst();
    }

    public boolean shouldDrop(FaultConfig fault) {
        if (fault.dropRate() == null || fault.dropRate() <= 0) {
            return false;
        }
        return ThreadLocalRandom.current().nextDouble(100) < fault.dropRate();
    }
}
