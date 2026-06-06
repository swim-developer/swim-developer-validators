package com.github.swim_developer.validator.core.infrastructure.console;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@ApplicationScoped
public class ConsoleNotificationService {

    private final CopyOnWriteArrayList<MultiEmitter<? super ConsoleEvent>> emitters = new CopyOnWriteArrayList<>();

    public void info(String message) {
        emit("info", message);
    }

    public void success(String message) {
        emit("success", message);
    }

    public void warning(String message) {
        emit("warning", message);
    }

    public void error(String message) {
        emit("error", message);
    }

    private void emit(String type, String message) {
        log.debug("Console event: [{}] {} -> {} listeners", type, message, emitters.size());
        ConsoleEvent event = new ConsoleEvent(type, message);
        Iterator<MultiEmitter<? super ConsoleEvent>> it = emitters.iterator();
        while (it.hasNext()) {
            MultiEmitter<? super ConsoleEvent> emitter = it.next();
            try {
                emitter.emit(event);
            } catch (Exception e) {
                log.trace("Removing failed emitter", e);
                emitters.remove(emitter);
            }
        }
    }

    public Multi<ConsoleEvent> getStream() {
        Multi<ConsoleEvent> events = Multi.createFrom().emitter(emitter -> {
            emitters.add(emitter);
            emitter.onTermination(() -> {
                log.debug("SSE connection terminated, removing emitter");
                emitters.remove(emitter);
            });
            emitter.emit(new ConsoleEvent("info", "Console connected"));
        });

        Multi<ConsoleEvent> heartbeat = Multi.createFrom()
                .ticks().every(Duration.ofSeconds(30))
                .map(tick -> new ConsoleEvent("heartbeat", ""));

        return Multi.createBy().merging().streams(events, heartbeat);
    }
}
