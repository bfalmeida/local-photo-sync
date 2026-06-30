# local-photo-sync - Task List

A command-line application to organize photos and videos by extracting dates from filenames and EXIF metadata.

## Project Overview

- **Language**: Java 17
- **Build Tool**: Maven
- **Framework**: Spring Boot 3.2.x (CLI only, no web)
- **Package**: `com.github.bfalmeida.photosync`
- **Version**: `1.0.0-STABLE`

---

## Task Phases

### Phase 1: Project Foundation

#### TASK-001: Initialize Maven Project
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Create the Maven project structure with Spring Boot configuration for CLI application.

**Acceptance Criteria**:
- [x] Project builds successfully
- [x] Standard Maven directory structure
- [x] Application entry point configured
- [x] Project compiles without errors
- [x] Application starts and exits cleanly

**Dependencies**: None

---

#### TASK-002: Configure Spring Shell CLI
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Set up interactive command-line interface for user input.

**Acceptance Criteria**:
- [x] Interactive shell prompt available when running app
- [x] Help command shows available commands
- [x] Shell exits cleanly with exit command

**Dependencies**: TASK-001

---

#### TASK-003: Configure Logging
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI

**Description**: 
Set up configurable logging with console output and optional file output.

**Acceptance Criteria**:
- [x] Log level configurable via configuration file and CLI flag
- [x] Console output with colored log levels
- [x] Optional file logging capability
- [x] Rolling file pattern for file logs

**Dependencies**: TASK-001

---

### Phase 2: Core CLI Commands

#### TASK-004: Define Sync Command Structure
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Define the main sync command with all required options for organizing media files.

**Acceptance Criteria**:
- [x] Command accepts source and destination paths
- [x] Dry-run mode enabled by default (preview before action)
- [x] Execute mode available to perform actual file operations
- [x] Options for handling files without dates
- [x] Logging level configurable
- [x] Optional file logging
- [x] Path validation before operation
- [x] Summary output showing copied, skipped, and error counts

**Dependencies**: TASK-002, TASK-003

---

#### TASK-004b: Make log-level and log-file optional
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI

**Description**: 
Make log-level and log-file parameters optional with sensible defaults.

**Acceptance Criteria**:
- [x] Log level defaults to INFO
- [x] Log file optional, defaults to console only

**Dependencies**: TASK-004

---

### Phase 3: Core Services

#### TASK-005: Implement File Scanner Service
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Scan source directory for media files (photos and videos) to be processed.

**Acceptance Criteria**:
- [x] Recursively scan source directory
- [x] Filter by supported media extensions for photos and videos
- [x] Stream-based processing for memory efficiency
- [x] Count total files scanned
- [x] Case-insensitive extension matching

**Dependencies**: TASK-004

---

#### TASK-005b: Implement File Listing (Scan Only)
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI

**Description**: 
Implement a scan-only mode to list files without copying.

**Acceptance Criteria**:
- [x] List all media files found in source directory
- [x] Display file information (name, size, date)

**Dependencies**: TASK-005

---

#### TASK-006: Implement Date Extraction Service (Filename Parsing)
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Extract dates from filenames to determine when photos and videos were taken.

**Acceptance Criteria**:
- [x] Support multiple date format patterns in filenames
- [x] Extract year and month from filename when present
- [x] Return empty when no date is parseable
- [x] Detect WhatsApp files from filename patterns
- [x] Identify media type (Photo/Video) from filename patterns
- [x] Comprehensive test coverage for all patterns

**Dependencies**: TASK-004

---

#### TASK-007: Implement EXIF Metadata Service
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Read and write EXIF metadata for date harmonization across image files.

**Acceptance Criteria**:
- [x] Read EXIF "Date Taken" from image files
- [x] Read video creation date from video files
- [x] Videos use filename date as fallback (never filesystem timestamp)
- [x] Update EXIF date to match filename date when available
- [x] Add EXIF date to images missing metadata when filename date exists
- [x] Handle files without EXIF gracefully
- [x] Skip EXIF operations for videos

**Dependencies**: TASK-006

---

#### TASK-008: Implement File Copy Service
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Copy files to destination with proper folder structure based on date and media type.

**Acceptance Criteria**:
- [x] Copy file to year/month/Photos or Videos folder
- [x] Place WhatsApp files in WhatsApp subfolder
- [x] Skip duplicate filenames silently without renaming
- [x] Create folder structure only when needed
- [x] Preserve file timestamps
- [x] Return operation result (success, skipped, error)

**Dependencies**: TASK-005, TASK-006, TASK-007

---

#### TASK-009: Implement Main Sync Orchestrator
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Coordinate all services to execute the complete media synchronization workflow.

**Acceptance Criteria**:
- [x] Orchestrate scanning, date extraction, EXIF handling, and file copying
- [x] Implement date resolution priority: filename, then EXIF, then filesystem
- [x] Handle undated files per CLI configuration
- [x] Collect and report statistics
- [x] Print summary at end of operation
- [x] Continue on non-fatal errors
- [x] Progress logging during operation

**Dependencies**: TASK-005, TASK-006, TASK-007, TASK-008

---

### Phase 3.5: Hardening & Refinements

#### TASK-014: Improve Null Handling
**Status**: DONE | **Priority**: MEDIUM | **Issue**: #30
**Description**: Improve null handling at CLI and parsing boundaries.
**Acceptance Criteria**:
- [x] All parsing boundaries handle nulls gracefully
- [x] CLI input validation prevents null propagation
**Dependencies**: TASK-004

---

#### TASK-015: Unify Media Path Logic
**Status**: DONE | **Priority**: MEDIUM | **Issue**: #33
**Description**: Unify Photos vs Videos path using `MediaType` (fix WMV etc.).
**Acceptance Criteria**:
- [x] Use `MediaType` enum for all path determination
- [x] Ensure WMV and other edge-case extensions are handled
**Dependencies**: TASK-008

---

#### TASK-016: Fix Destination Path Previews
**Status**: DONE | **Priority**: MEDIUM | **Issue**: #32
**Description**: Fix destination path preview: undated folder must not override dated layout.
**Acceptance Criteria**:
- [x] Path preview shows correct destination even for undated files
- [x] No override of dated layout in preview output
**Dependencies**: TASK-008

---

#### TASK-017: Implement `skipUndated` Filtering
**Status**: DONE | **Priority**: LOW | **Issue**: #34
**Description**: Honor `skipUndated` in sync listing (and future copy).
**Acceptance Criteria**:
- [x] Files without dates are excluded when `skipUndated` is true
- [x] Listing and copy operations respect the flag
**Dependencies**: TASK-005

---

#### TASK-018: Sync WhatsApp Folder Logic
**Status**: DONE | **Priority**: MEDIUM | **Issue**: #37
**Description**: Add WhatsApp subfolder to destination paths (docs parity).
**Acceptance Criteria**:
- [x] Destination paths include WhatsApp subfolder where applicable
- [x] Logic matches the project documentation
**Dependencies**: TASK-008

---

#### TASK-019: Fix Logback Configuration
**Status**: DONE | **Priority**: LOW | **Issue**: #36
**Description**: Fix Logback default profile: root references undefined FILE/ERROR_FILE appenders.
**Acceptance Criteria**:
- [x] Appenders correctly defined in `logback-spring.xml`
- [x] No warnings about undefined appenders on startup
**Dependencies**: TASK-003

---

#### TASK-020: Integrate JaCoCo Coverage
**Status**: DONE | **Priority**: MEDIUM | **Issue**: #35
**Description**: Add JaCoCo to Maven build (match docs/TESTING.md coverage target).
**Acceptance Criteria**:
- [x] JaCoCo plugin configured in `pom.xml`
- [x] Coverage reports generated after running tests
**Dependencies**: TASK-011

---

#### TASK-021: Enhance DateInfo Testing
**Status**: DONE | **Priority**: LOW | **Issue**: #38
**Description**: Strengthen `DateInfoTest`: undated-folder case should exercise `DateInfo` / `SyncCommand`.
**Acceptance Criteria**:
- [x] New test cases for undated folder scenarios
- [x] Integration between `DateInfo` and `SyncCommand` verified for undated files
**Dependencies**: TASK-011

---

### Phase 4: Quality & Testing

#### TASK-010: Create Test Dataset
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI

**Description**: 
Create sample media files for testing the synchronization workflow.

**Acceptance Criteria**:
- [x] Multiple images with different filename date formats
- [x] Images with EXIF dates matching filenames
- [x] Images with EXIF dates not matching filenames (for harmonization)
- [x] Images without EXIF data
- [x] Video files for testing
- [x] WhatsApp files (photos and videos)
- [x] Files without parseable dates

**Dependencies**: TASK-007

---

#### TASK-011: Write Unit Tests
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
Write comprehensive tests for all services to ensure correct behavior.

**Acceptance Criteria**:
- [x] Tests for date pattern extraction from filenames
- [x] Tests for EXIF read/update scenarios
- [x] Tests for file copying and duplicate handling
- [x] Tests for folder structure creation
- [x] Tests for file discovery
- [x] Tests for full synchronization workflow
- [x] Minimum 80% line coverage across all services

**Dependencies**: TASK-010

---

### Phase 5: Documentation

#### TASK-012: Update README.md
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI

**Description**: 
Write comprehensive user documentation with usage examples.

**Acceptance Criteria**:
- [x] Build and run instructions
- [x] All CLI options documented
- [x] Example commands with expected output
- [x] Configuration options explained
- [x] System requirements listed

**Dependencies**: TASK-004

---

#### TASK-013: Final Integration Test
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI

**Description**: 
End-to-end verification of the complete synchronization workflow.

**Acceptance Criteria**:
- [x] Dry-run produces no file changes
- [x] Execute creates correct directory structure
- [x] Files placed in correct year/month folders
- [x] Photos and Videos correctly separated
- [x] WhatsApp files in WhatsApp subfolder
- [x] Duplicate files skipped silently
- [x] EXIF harmonization verified
- [x] Undated files handled per configuration
- [x] Summary statistics accurate

**Dependencies**: TASK-009, TASK-011

---

### Phase 6: Content-Addressable Sync (Valkey)

#### TASK-022: Implement Hashing Service
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI
**Description**: Implement SHA-256 hashing for file content identification.

#### TASK-023: Integrate Valkey State Store
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI
**Description**: Use Valkey/Redis to track processed files and prevent redundant operations.

#### TASK-024: Implement Atomic Safe-Move Transport
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI
**Description**: Ensure file moves are atomic to prevent data loss during crashes.

#### TASK-025: Implement Sync State Validation
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI
**Description**: Verify state consistency between Valkey and filesystem.

#### TASK-026: Optimize State Lookups
**Status**: DONE | **Priority**: MEDIUM | **Assignee**: AI
**Description**: Improve performance of duplicate checks using Valkey sets.

#### TASK-027: Implement State Cleanup Utility
**Status**: DONE | **Priority**: LOW | **Assignee**: AI
**Description**: Provide CLI command to clear or prune the sync state.

#### TASK-028: Final Validation of Content-Addressable Sync
**Status**: DONE | **Priority**: HIGH | **Assignee**: AI
**Description**: End-to-end test of SHA-256 sync workflow.

---

## Task Dependencies

```
TASK-001 (Project Init)
         ↓
TASK-002 (Shell)         TASK-003 (Logging)
         ↓                       ↓
TASK-004 (CLI Command)
         ↓
TASK-005 (File Scanner) ─┐─→ TASK-009 (Orchestrator)
TASK-006 (Date Extract)   │          ↓
TASK-007 (EXIF Service)   │   TASK-010 (Test Data)
TASK-008 (File Copy)      │          ↓
                           │   TASK-011 (Unit Tests)
                           │          ↓
                           └─→  TASK-012 (README)
                                     TASK-013 (Integration)
                                     ↓
                             PHASE 6: VALKEY INTEGRATION
                             TASK-022 → TASK-023 → TASK-024 → TASK-025 → TASK-026 → TASK-027 → TASK-028
```

---

## Project Metadata

| Property | Value |
|----------|-------|
| Language | Java 17 |
| Build Tool | Maven |
| Framework | Spring Boot 3.2.x |
| CLI | Spring Shell |
| Package | com.github.bfalmeida.photosync |
| Version | 1.0.0-STABLE |
| Coverage Target | 80%+ |

---

## Default Behaviors

- **Dry-run mode**: Enabled by default for safety
- **Duplicate handling**: Skip silently without renaming
- **Undated files**: Moved to undated folder (configurable)
- **WhatsApp detection**: Files with WA in filename after date portion

---

## Version History

| Date | Version | Notes |
|------|---------|-------|
| 2026-03-22 | 1.0.0 | Initial functional task list created |
| 2026-03-29 | 1.0.0 | Updated task statuses - Phase 1-3 complete |
| 2026-06-12 | 1.0.0 | Added Valkey Integration Phase (Phase 6) |
| 2026-06-30 | 1.0.0-STABLE | Synchronized backlog with reality; marked all v1 features DONE |

---

## Notes for Contributors

1. **Start with TASK-001**: Project initialization must be completed first
2. **One task at a time**: Complete each task before moving to the next
3. **Test coverage**: Aim for 80%+ line coverage on all services
4. **Dry-run default**: Always test with dry-run mode first
5. **Log at DEBUG level**: Add debug logging for non-trivial logic
6. **WhatsApp detection**: Based on WA pattern in filename after date
7. **Videos use filename**: Video EXIF extraction is tried but filename is always fallback
