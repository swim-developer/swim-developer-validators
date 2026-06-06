# swim-validator-core

Shared infrastructure for all SWIM validators.

## What it provides

- **Fault injection**, `FaultInjectionService` adds runtime faults (HTTP status overrides, delays, request drops) for chaos testing of SWIM services
- **Console notifications**, `ConsoleNotificationService` streams typed events (info, success, warning, error) via Server-Sent Events for real-time validator UIs
- **XML randomizers**, utilities that randomize dates, coordinates, and IDs in AIXM and ED-254 XML samples to generate realistic test data
- **Common DTOs**, subscription responses, topic summaries, error responses shared across all validators
