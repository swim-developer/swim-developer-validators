package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.github.swim_developer.validator.consumer.domain.port.in.XmlFileCachePort;

@Slf4j
@ApplicationScoped
public class XmlEventFileLoader implements XmlFileCachePort {

    @ConfigProperty(name = "event.generator.events.path", defaultValue = "/opt/events")
    String eventsPath;

    public List<Path> listXmlFiles() throws IOException {
        Path dir = Paths.get(eventsPath);
        if (!Files.exists(dir)) {
            log.error("Events directory does not exist: {}", eventsPath);
            return List.of();
        }
        return Files.list(dir)
                .filter(path -> path.toString().endsWith(".xml"))
                .toList();
    }

    public Path selectRandomFile(List<Path> files) {
        return files.get(ThreadLocalRandom.current().nextInt(files.size()));
    }

    public String readEventFile(String filename) {
        try {
            Path filePath = Paths.get(eventsPath, filename);
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("Event file not found: " + filename);
            }
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read event file: " + filename, e);
        }
    }

    @CacheResult(cacheName = "event-files")
    public List<String> getCachedXmlContent() {
        log.debug("Refreshing/Loading XML cache from path: {}", eventsPath);
        try {
            return listXmlFiles().stream()
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Critical error loading XML files into cache", e);
            return List.of();
        }
    }

    @CacheInvalidate(cacheName = "event-files")
    public void clearXmlCache() {
        log.debug("XML cache cleared");
    }

    public String getEventsPath() {
        return eventsPath;
    }
}
