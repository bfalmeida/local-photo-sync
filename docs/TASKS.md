# local-photo-sync - Task List

A command-line application to organize photos and videos by extracting dates from filenames and EXIF metadata.

## Project Overview

- **Language**: Java 21
- **Build Tool**: Maven
- **Framework**: Spring Boot 3.2.x (CLI only, no web)
- **Package**: `com.github.bfalmeida.photosync`
- **Version**: `1.0.0-SNAPSHOT`

---

## Task Phases

### Phase 1: Project Foundation

#### TASK-001: Initialize Maven Project
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Create the Maven project structure with Spring Boot configuration for CLI application.

**Acceptance Criteria**:
- [ ] Project builds successfully
- [ ] Standard Maven directory structure
- [ ] Application entry point configured
- [ ] Project compiles without errors
- [ ] Application starts and exits cleanly

**Dependencies**: None

---

#### TASK-002: Configure Spring Shell CLI
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Set up interactive command-line interface for user input.

**Acceptance Criteria**:
- [ ] Interactive shell prompt available when running app
- [ ] Help command shows available commands
- [ ] Shell exits cleanly with exit command

**Dependencies**: TASK-001

---

#### TASK-003: Configure Logging
**Status**: TODO | **Priority**: MEDIUM | **Assignee**: TBD

**Description**: 
Set up configurable logging with console output and optional file output.

**Acceptance Criteria**:
- [ ] Log level configurable via configuration file and CLI flag
- [ ] Console output with colored log levels
- [ ] Optional file logging capability
- [ ] Rolling file pattern for file logs

**Dependencies**: TASK-001

---

### Phase 2: Core CLI Commands

#### TASK-004: Define Sync Command Structure
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Define the main sync command with all required options for organizing media files.

**Acceptance Criteria**:
- [ ] Command accepts source and destination paths
- [ ] Dry-run mode enabled by default (preview before action)
- [ ] Execute mode available to perform actual file operations
- [ ] Options for handling files without dates
- [ ] Logging level configurable
- [ ] Optional file logging
- [ ] Path validation before operation
- [ ] Summary output showing copied, skipped, and error counts

**Dependencies**: TASK-002, TASK-003

---

### Phase 3: Core Services

#### TASK-005: Implement File Scanner Service
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Scan source directory for media files (photos and videos) to be processed.

**Acceptance Criteria**:
- [ ] Recursively scan source directory
- [ ] Filter by supported media extensions for photos and videos
- [ ] Stream-based processing for memory efficiency
- [ ] Count total files scanned
- [ ] Case-insensitive extension matching

**Dependencies**: TASK-004

---

#### TASK-006: Implement Date Extraction Service (Filename Parsing)
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Extract dates from filenames to determine when photos and videos were taken.

**Acceptance Criteria**:
- [ ] Support multiple date format patterns in filenames
- [ ] Extract year and month from filename when present
- [ ] Return empty when no date is parseable
- [ ] Detect WhatsApp files from filename patterns
- [ ] Identify media type (Photo/Video) from filename patterns
- [ ] Comprehensive test coverage for all patterns

**Dependencies**: TASK-004

---

#### TASK-007: Implement EXIF Metadata Service
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Read and write EXIF metadata for date harmonization across image files.

**Acceptance Criteria**:
- [ ] Read EXIF "Date Taken" from image files
- [ ] Read video creation date from video files
- [ ] Videos use filename date as fallback (never filesystem timestamp)
- [ ] Update EXIF date to match filename date when available
- [ ] Add EXIF date to images missing metadata when filename date exists
- [ ] Handle files without EXIF gracefully
- [ ] Skip EXIF operations for videos

**Dependencies**: TASK-006

---

#### TASK-008: Implement File Copy Service
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Copy files to destination with proper folder structure based on date and media type.

**Acceptance Criteria**:
- [ ] Copy file to year/month/Photos or Videos folder
- [ ] Place WhatsApp files in WhatsApp subfolder
- [ ] Skip duplicate filenames silently without renaming
- [ ] Create folder structure only when needed
- [ ] Preserve file timestamps
- [ ] Return operation result (success, skipped, error)

**Dependencies**: TASK-005, TASK-006, TASK-007

---

#### TASK-009: Implement Main Sync Orchestrator
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Coordinate all services to execute the complete media synchronization workflow.

**Acceptance Criteria**:
- [ ] Orchestrate scanning, date extraction, EXIF handling, and file copying
- [ ] Implement date resolution priority: filename, then EXIF, then filesystem
- [ ] Handle undated files per CLI configuration
- [ ] Collect and report statistics
- [ ] Print summary at end of operation
- [ ] Continue on non-fatal errors
- [ ] Progress logging during operation

**Dependencies**: TASK-005, TASK-006, TASK-007, TASK-008

---

### Phase 4: Quality & Testing

#### TASK-010: Create Test Dataset
**Status**: TODO | **Priority**: MEDIUM | **Assignee**: TBD

**Description**: 
Create sample media files for testing the synchronization workflow.

**Acceptance Criteria**:
- [ ] Multiple images with different filename date formats
- [ ] Images with EXIF dates matching filenames
- [ ] Images with EXIF dates not matching filenames (for harmonization)
- [ ] Images without EXIF data
- [ ] Video files for testing
- [ ] WhatsApp files (photos and videos)
- [ ] Files without parseable dates

**Dependencies**: TASK-007

---

#### TASK-011: Write Unit Tests
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
Write comprehensive tests for all services to ensure correct behavior.

**Acceptance Criteria**:
- [ ] Tests for date pattern extraction from filenames
- [ ] Tests for EXIF read/update scenarios
- [ ] Tests for file copying and duplicate handling
- [ ] Tests for folder structure creation
- [ ] Tests for file discovery
- [ ] Tests for full synchronization workflow
- [ ] Minimum 80% line coverage across all services

**Dependencies**: TASK-010

---

### Phase 5: Documentation

#### TASK-012: Update README.md
**Status**: TODO | **Priority**: MEDIUM | **Assignee**: TBD

**Description**: 
Write comprehensive user documentation with usage examples.

**Acceptance Criteria**:
- [ ] Build and run instructions
- [ ] All CLI options documented
- [ ] Example commands with expected output
- [ ] Configuration options explained
- [ ] System requirements listed

**Dependencies**: TASK-004

---

#### TASK-013: Final Integration Test
**Status**: TODO | **Priority**: HIGH | **Assignee**: TBD

**Description**: 
End-to-end verification of the complete synchronization workflow.

**Acceptance Criteria**:
- [ ] Dry-run produces no file changes
- [ ] Execute creates correct directory structure
- [ ] Files placed in correct year/month folders
- [ ] Photos and Videos correctly separated
- [ ] WhatsApp files in WhatsApp subfolder
- [ ] Duplicate files skipped silently
- [ ] EXIF harmonization verified
- [ ] Undated files handled per configuration
- [ ] Summary statistics accurate

**Dependencies**: TASK-009, TASK-011

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
```

---

## Project Metadata

| Property | Value |
|----------|-------|
| Language | Java 21 |
| Build Tool | Maven |
| Framework | Spring Boot 3.2.x |
| CLI | Spring Shell |
| Package | com.github.bfalmeida.photosync |
| Version | 1.0.0-SNAPSHOT |
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

---

## Notes for Contributors

1. **Start with TASK-001**: Project initialization must be completed first
2. **One task at a time**: Complete each task before moving to the next
3. **Test coverage**: Aim for 80%+ line coverage on all services
4. **Dry-run default**: Always test with dry-run mode first
5. **Log at DEBUG level**: Add debug logging for non-trivial logic
6. **WhatsApp detection**: Based on WA pattern in filename after date
7. **Videos use filename**: Video EXIF extraction is tried but filename is always fallback
