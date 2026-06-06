# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
make sync              # full setup: pull + clone deps + install deps
make install           # build + install all modules to local Maven repo (skip tests)
make test              # unit + integration tests for all modules
make sonar             # SonarQube analysis (requires SONAR_TOKEN)
make security-deps     # OWASP dependency-check

./mvnw clean install -DskipTests          # equivalent to make install
./mvnw verify -DskipITs=false             # equivalent to make test

# single module
./mvnw test -pl swim-validator-core
./mvnw test -pl swim-validator-consumer
./mvnw test -pl swim-validator-provider

# single test class
./mvnw test -pl swim-validator-core -Dtest=SomeTestClass

# coverage
./mvnw test jacoco:report                 # report at target/site/jacoco/index.html
```

Requires sibling projects `swim-developer-root` and `swim-developer-framework` installed in local Maven repo (use `make sync` on first setup).

## Architecture

Multi-module Maven project (parent POM) providing shared infrastructure for SWIM service validators. Quarkus-based, Java, Lombok.

### Module Dependency

```
swim-validator-core
    ↑               ↑
swim-validator-consumer    swim-validator-provider
```

### Module Responsibilities

- **swim-validator-core** — Shared domain models (TopicData, SubscriptionStatus, QualityOfService), fault injection engine (FaultInjectionService/Filter), console notifications via SSE (ConsoleNotificationService), XML randomizers, REST root resource, common DTOs.
- **swim-validator-consumer** — Simulates a SWIM provider (EAD/AISP) to test consumer implementations. Manages subscriptions (SubscriptionService), AMQP event generation (EventGeneratorService), heartbeat publishing, load testing, scenario previews. Uses Vert.x AMQP client, Hibernate ORM + Panache with MariaDB, Quarkus Scheduler.
- **swim-validator-provider** — Simulates a SWIM consumer to test provider implementations. AMQP connection state management (UserConnectionState), JWT parsing (JwtService), conformance test framework (TestScenario, TestResult, AssertionResult), mTLS HTTP proxy. Same persistence stack as consumer module.

### Domain Architecture (Hexagonal / Ports & Adapters)

Each module follows this layout:
- `domain/model/` — entities, value objects
- `domain/port/in/` — inbound ports (use case interfaces)
- `domain/port/out/` — outbound ports (repository interfaces)
- `domain/util/` — domain utilities (e.g., XmlPatternExtractor)
- `application/usecase/` — use case implementations
- `infrastructure/messaging/` — AMQP adapters (Vert.x AMQP client)
- `infrastructure/persistence/` — JPA repositories, mappers, entity classes
- `infrastructure/rest/` — JAX-RS resources and DTOs
- `infrastructure/fault/` — fault injection (core module)
- `infrastructure/console/` — SSE console notifications (core module)

### Downstream Validator Applications (separate repos)

This project is the **parent POM and shared library** for these standalone applications:
- `swim-dnotam-consumer-validator` — fake provider that tests DNOTAM consumer implementations
- `swim-dnotam-provider-validator` — fake consumer that tests DNOTAM provider implementations
- `swim-ed254-consumer-validator` — fake provider that tests ED-254 consumer implementations
- `swim-ed254-provider-validator` — fake consumer that tests ED-254 provider implementations

Each validator declares `swim-validators` as parent and depends on either `swim-validator-consumer` or `swim-validator-provider`.

### Infrastructure

- `infrastructure/artemis/` — ActiveMQ Artemis broker configuration (broker.xml, SASL/certificate auth, Containerfile)

## Key Conventions

- **Naming**: every artifact name must be unambiguous and qualified (e.g., `swim-dnotam-consumer`, not just `swim-consumer`).
- **Consumer-Validator rule**: a Consumer connects to its Consumer-Validator (fake provider), never to the actual Provider of the same module.
