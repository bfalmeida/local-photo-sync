# UI Implementation Backlog: local-photo-sync

This document decomposes the `TDD_UI.md` into atomic Micro-Sprints. Each task is designed to be completed by a Code Cadet in under 600 seconds.

## Sprint 1: Project Infrastructure
**Goal:** Establish the Compose for Desktop environment and bridge it with the existing Spring Boot backend.

| Task ID | Task Description | Acceptance Criteria |
| :--- | :--- | :--- |
| 1.1 | **Gradle Compose Setup** | `build.gradle.kts` updated with Compose Multiplatform plugins and dependencies; project builds successfully. |
| 1.2 | **Basic Window Shell** | A `Main.kt` entry point that launches a basic window with a "Hello local-photo-sync" label. |
| 1.3 | **Spring Context Bridge** | Implementation of a `SpringContext` utility class to allow static access to Spring beans from Compose views. |
| 1.4 | **Context Integration Test** | UI displays the version or a property from a Spring-managed service (e.g., `SyncService`) upon startup. |

## Sprint 2: Configuration UI
**Goal:** Implement all user-configurable settings and input validation.

| Task ID | Task Description | Acceptance Criteria |
| :--- | :--- | :--- |
| 2.1 | **Source Path Picker** | UI for source directory selection using native folder dialog; path saved to state. |
| 2.2 | **Destination Path Picker** | UI for destination directory selection using native folder dialog; path saved to state. |
| 2.3 | **Path Validation Logic** | UI displays error if paths are empty, read-only, or if destination is a child of source. |
| 2.4 | **Valkey Settings UI** | Input fields for Host, Port, and Password with basic format validation. |
| 2.5 | **Valkey Connection Test** | "Test Connection" button that calls `ValkeyStateService` and shows Success/Failure toast. |
| 2.6 | **Performance Tuning UI** | Numeric inputs or sliders for Thread Count and Batch Size; values bounded to reasonable limits. |
| 2.7 | **Sync Strategy Selector** | Dropdown menu for selecting sync modes (Mirror, Merge); selection saved to state. |

## Sprint 3: Execution Logic
**Goal:** Connect the UI configuration to the core sync engine via a ViewModel.

| Task ID | Task Description | Acceptance Criteria |
| :--- | :--- | :--- |
| 3.1 | **SyncViewModel Implementation** | Create `SyncViewModel` using `StateFlow` to track all configuration and execution states. |
| 3.2 | **Dry-Run Toggle** | Checkbox for "Dry Run" mode; state passed to `SyncService` during execution. |
| 3.3 | **Execute Button State** | "Execute" button is disabled until all required paths and Valkey settings are validated. |
| 3.4 | **Sync Trigger Integration** | Clicking "Execute" invokes `SyncService.startSync()` within `viewModelScope` on `Dispatchers.IO`. |
| 3.5 | **Cancel/Pause Logic** | "Cancel" button appears during sync; invokes `SyncService.stop()` and resets UI state. |

## Sprint 4: Progress Dashboard
**Goal:** Provide real-time visibility into the synchronization process.

| Task ID | Task Description | Acceptance Criteria |
| :--- | :--- | :--- |
| 4.1 | **Global Progress Bar** | Visual progress bar updated in real-time based on percentage emitted by `SyncService`. |
| 4.2 | **Statistics Panel** | Labels showing: Total files scanned, Total size transferred, and Elapsed time. |
| 4.3 | **Virtualized File Log** | `LazyColumn` implementation showing a scrollable list of processed files to prevent memory bloat. |
| 4.4 | **Real-time Log Wiring** | ViewModel collects the file-by-file `Flow` from `SyncService` and updates the `LazyColumn` state. |
| 4.5 | **Log Status Styling** | Files in the log are color-coded by status: Green (Synced), Grey (Skipped), Red (Error). |

## Sprint 5: Native Hardening
**Goal:** Package the application into a high-performance native binary using GraalVM.

| Task ID | Task Description | Acceptance Criteria |
| :--- | :--- | :--- |
| 5.1 | **GraalVM Toolchain Setup** | Project configured to use GraalVM JDK; `native-image` tool available in build environment. |
| 5.2 | **Spring AOT Configuration** | Spring Boot AOT processing enabled in Gradle to optimize native startup and memory. |
| 5.3 | **Reflection Config Gen** | Run application with GraalVM tracing agent to generate `reflect-config.json` for Compose and Spring. |
| 5.4 | **Native Binary Build** | Successful execution of the native image build task producing a standalone executable. |
| 5.5 | **Native Binary Verification** | Native executable launches and performs a basic sync operation without `ClassNotFound` or `NoSuchMethod` errors. |
