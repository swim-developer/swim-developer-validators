# swim-validator-consumer

Base module for consumer-side validators. Provides the subscription management, AMQP event generation, and persistence infrastructure that DNOTAM and ED-254 consumer validators extend.

## What it provides

- **Subscription management**, `SubscriptionService` with create, renew, pause/resume, delete operations and queue name generation
- **AMQP connection management**, `AmqpConnectionManager` using Vert.x AMQP client with reconnection logic
- **Event generation**, `EventGeneratorService` with scheduled and manual event publishing, date/location randomization
- **Heartbeat publishing**, periodic heartbeat messages to subscription heartbeat queues
- **Load testing**, `LoadTestService` for high-frequency event injection
- **Persistence**, Hibernate/Panache with MariaDB for subscription state
- **Health checks**, AMQP connection health probe
