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
