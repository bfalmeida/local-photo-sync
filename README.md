# local-photo-sync

A command-line application to organize photos and videos by extracting dates from filenames and EXIF metadata.

Application being developed by AI.

## Features

- Organizes files by year and month
- Separates photos and videos into different folders
- Detects and organizes WhatsApp media files
- Extracts dates from filenames (primary) or EXIF metadata (fallback)
- Dry-run mode for safe previews
- Interactive CLI powered by Spring Shell

## Progress

- [x] TASK-001: Initialize Maven Project
- [x] TASK-002: Configure Spring Shell CLI
- [x] TASK-003: Configure Logging
- [x] TASK-004: Define Sync Command Structure
- [x] TASK-004b: Make log-level and log-file optional
- [x] TASK-005: Implement File Scanner Service
- [x] TASK-005b: Implement File Listing (Scan Only)
- [x] TASK-006: Implement Date Extraction Service
- [x] TASK-007: Implement EXIF Metadata Service
- [x] TASK-008: Implement File Copy Service
- [ ] TASK-009: Implement Main Sync Orchestrator
- [ ] TASK-010: Create Test Dataset
- [ ] TASK-011: Write Unit Tests
- [ ] TASK-012: Update README.md
- [ ] TASK-013: Final Integration Test

## Requirements

- Java 21
- Maven 3.9+

## Quick Start

```bash
# Build
mvn clean package

# Run
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar
```

## Documentation

See the [docs/](docs/) directory for detailed documentation:

- [Architecture](docs/ARCHITECTURE.md) - System design and service responsibilities
- [CLI Reference](docs/CLI.md) - Command usage and options
- [Destination Structure](docs/DESTINATION_STRUCTURE.md) - How files are organized
- [Testing Guidelines](docs/TESTING.md) - Testing strategy and coverage targets
- [Task List](docs/TASKS.md) - Implementation tasks

## License

MIT
