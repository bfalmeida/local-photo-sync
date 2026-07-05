# Valkey Persistence Integration

## Overview
The system uses Valkey (Redis-compatible) to track processed files and prevent duplicates across multiple synchronization sessions.

## Architectural Implementation
The persistence layer is decoupled via the `SyncStateRepository` interface. The `ValkeyStateService` acts as the la-Adapter for this interface.

### State Schema
- **Session Data**: `session:{sessionId}` $\to$ Hash containing source, destination, and status.
- **Processed Files**: `session:{sessionId}:processed` $\to$ Set of relative paths.
- **Global Hashes**: `hashes:global` $\to$ Set of all processed file hashes.
- **Progress**: `session:{sessionId}:last_file` $\to$ String of the last processed file.

## Configuration
Valkey settings are managed in `application.yml`:

```yaml
valkey:
  host: 192.168.0.132
  port: 6379
  password: your_password_here
```

## Health Monitoring
The system performs a non-destructive `PING` every 30 seconds to verify connectivity. If the connection is lost, the UI Heartbeat indicator will turn **Red**, and logs will notify the user.
