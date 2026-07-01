# local-photo-sync - Task List

## Project Overview

- **Language**: Kotlin 1.9.23
- **Build Tool**: Gradle 8.7
- **Framework**: Spring Boot 3.2.x (with Compose for Desktop UI)
- **Package**: `com.github.bfalmeida.photosync`
- **Version**: `v1.1.0-BETA`
- **Coverage Target**: 80%+

---

## Task Phases

### Phase 1-3: Project Foundation & Core CLI (Legacy)
*These tasks were completed during the prototype phase (v1.0.0).*
- [x] TASK-001: Initialize Maven Project (Now migrated to Gradle)
- [x] TASK-002: Configure Spring Shell CLI (Now Deprecated)
- [x] TASK-003: Configure Logging
- [x] TASK-004: Define Sync Command Structure (Logic now migrated to UI)

### Phase 4-5: Core Services & Quality
- [x] TASK-005: Implement File Scanner Service
- [x] TASK-006: Implement Date Extraction Service (Filename Parsing)
- [x] TASK-007: Implement EXIF Metadata Service
- [x] TASK-008: Implement File Copy Service
- [x] TASK-009: Implement Main Sync Orchestrator
- [x] TASK-010: Create Test Dataset
- [x] TASK-011: Write Unit Tests
- [x] TASK-012: Update Documentation

### Phase 6: Content-Addressable Sync (Valkey)
- [x] TASK-022: Implement Hashing Service (SHA-256)
- [x] TASK-023: Integrate Valkey State Store
- [x] TASK-024: Implement Atomic Safe-Move Transport
- [x] TASK-025: Implement Sync State Validation
- [x] TASK-026: Optimize State Lookups
- [x] TASK-027: Implement State Cleanup Utility
- [x] TASK-028: Final Validation of Content-Addressable Sync

### Phase 7: The Interface Leap & Great Unification [COMPLETED]
*The transition from a CLI prototype to a professional Desktop Application.*
- [x] **UI Infrastructure**: Setup Compose for Desktop and Spring Context Bridge.
- [x] **Configuration UI**: Implementation of path pickers and Valkey settings.
- [x] **Execution Logic**: MVVM integration via SyncViewModel and StateFlow.
- [x] **Progress Dashboard**: Real-time statistics and virtualized activity log.
- [x] **The Great Unification**: Total migration of all Java services to Kotlin.
- [x] **Build System Shift**: Migration from Maven to Gradle for framework compatibility.
- [x] **Feature Parity**: Implementation of Dry-Run, Undated Folder, and Skip-Undated toggles in UI.

---

## Project Metadata

| Property | Value |
|----------|-------|
| Language | Kotlin 1.9.23 |
| Build Tool | Gradle 8.7 |
| Framework | Spring Boot 3.2.x |
| Interface | Compose for Desktop |
| Package | `com.github.bfalmeida.photosync` |
| Version | `v1.1.0-BETA` |
| Coverage Target | 80%+ |

---

## Default Behaviors

- **Dry-run mode**: Enabled by default for safety.
- **Duplicate handling**: Skip silently without renaming.
- **Undated files**: Moved to undated folder (configurable via UI).
- **WhatsApp detection**: Files with WA in filename after date portion.

---

## Version History

| Date | Version | Notes |
|------|---------|-------|
| 2026-03-22 | 1.0.0 | Initial functional task list created |
| 2026-06-12 | 1.0.0 | Added Valkey Integration Phase (Phase 6) |
| 2026-06-30 | 1.1.0-BETA | Unified Kotlin Core, Compose UI, and Gradle Migration complete |
