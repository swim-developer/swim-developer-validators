package com.github.swim_developer.validator.consumer.infrastructure.health;

import com.github.swim_developer.validator.consumer.domain.port.in.EventGeneratorPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class AmqpConnectionHealthCheck implements HealthCheck {

    private final EventGeneratorPort eventGeneratorService;

    @Inject
    public AmqpConnectionHealthCheck(EventGeneratorPort eventGeneratorService) {
        this.eventGeneratorService = eventGeneratorService;
    }

    @Override
    public HealthCheckResponse call() {
        boolean connected = eventGeneratorService.isAmqpConnected();

        return HealthCheckResponse.named("amqp-connection")
                .status(connected)
                .withData("broker", eventGeneratorService.getBrokerInfo())
                .build();
    }
}
