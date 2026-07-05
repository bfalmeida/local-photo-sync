# Architecture Blueprint - Local Photo Sync

## 1. Architectural Pattern: Clean Architecture (Hexagonal)
The system is designed as a set of decoupled layers to ensure testability and flexibility.

### Layer Responsibilities
| Layer | Responsibility | Key Components |
| :--- | :--- | :--- |
| **View (UI)** | Pure rendering and input capture. No business logic. | `MainWindow`, `SyncDashboardPanel`, `SyncConfigPanel`, `HeartbeatIndicator` |
| **Orchestration** | Manages threading and bridges telemetry to the UI. | `SyncController` |
| **Domain (Core)** | Pure synchronization logic and rules. Agnostic of UI and Storage. | `SyncService`, `MediaFileScanner`, `FilenameDateExtractor` |
| **Persistence (SPI)** | Defines the contract for state management. | `SyncStateRepository` (Interface) |
| **Infrastructure** | Specific implementation of the persistence contract. | `ValkeyStateService` |

## 2. Telemetry & Communication: Observer Pattern
To avoid tight coupling between the core engine and the UI, the system utilizes a **SyncEventBus**.

### Event Flow
`SyncService` $\to$ `SyncEventBus` $\to$ `SyncController` $\to$ `MainWindow`

### Event Types
- **PROGRESS**: Percentage and counts (Copied/Skipped/Errors).
- **LOG**: Human-readable status messages.
- **COMPLETE**: Final summary of the session.
- **ERROR**: Critical failure alerts.
- **HEALTH**: System connectivity and disk space status.

## 3. Health Monitoring System
The system implements a proactive "Heartbeat" monitor to ensure dependency availability.

### Logic Flow
`@Scheduled (30s)` $\to$ `HealthMonitorService` $\to$ `SyncStateRepository.ping()` $\to$ `SyncEventBus` $\to$ `MainWindow`

### Visual Indicators
- **Green**: System healthy.
- **Yellow**: Healthy but with warnings (e.g., Disk > 80% full).
- **Red**: Critical failure (e.g., Valkey Offline).

## 4. Data Models
- **SyncSettings**: Immutable record consolidating all configuration parameters.
- **HealthStatus**: Immutable record representing the state of a dependency.
- **SyncStatistics**: Mutable accumulator for session metrics.
