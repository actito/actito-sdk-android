# CHANGELOG

## 5.0.0

- Updated to Kotlin 2.0
- Improved network request retry mechanism
- Exposed Firebase Messaging handlers
- Added name and size restrictions to tag names, event names and event payloads.
- Crash reporting is deprecated and disabled by default. We recommend using another solution to collect crash analytics.
- Removed Java compatibility classes `*Compat` in favor of Java-compatible module implementations.

Prior to upgrading to v5.x, consult the [Migration Guide](./MIGRATION.md), which outlines all necessary changes and procedures to ensure a smooth migration.
