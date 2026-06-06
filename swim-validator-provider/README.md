# swim-validator-provider

Base module for provider-side validators. Provides the conformance testing framework, mTLS HTTP proxy, and AMQP message capture that DNOTAM and ED-254 provider validators extend.

## What it provides

- **Conformance test framework**, test scenario registry, assertion helpers, test result tracking with request/response capture
- **mTLS HTTP proxy**, proxies requests to the provider REST API with mutual TLS for subscription management testing
- **JWT parsing**, `JwtService` for extracting user identity from Bearer tokens
- **AMQP message capture**, per-user connection tracking, receiver lifecycle management, SSL configuration
- **Persistence**, Hibernate/Panache with MariaDB for subscriptions, user AMQP configs, and received messages
- **Common DTOs**, test results, assertions, AMQP status, proxy request/response models
