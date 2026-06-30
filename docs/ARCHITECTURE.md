# local-photo-sync - Architecture

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.23 |
| Build Tool | Gradle 8.7 |
| Framework | Spring Boot 3.2.x |
| Interface | Compose for Desktop |
| EXIF Processing | metadata-extractor library |
| Testing | JUnit 5 + AssertJ |
| Code Coverage | JaCoCo |

## Package Organization

```
com.github.bfalmeida.photosync
├── cli/          # Legacy CLI components (Deprecated)
├── service/     # Business logic services (Unified Kotlin)
├── model/       # Domain objects and state
└── ui/          # Compose for Desktop UI & ViewModels
```

## Service Responsibilities

### File Scanner Service
- Discovers media files in the source directory
- Filters files by supported media extensions
- Processes files using streams for memory efficiency

### Date Extraction Service
- Extracts dates from filenames using pattern matching
- Identifies media type (Photo/Video) from filename patterns
- Detects WhatsApp files based on filename characteristics

### EXIF Metadata Service
- Reads date metadata from image files
- Reads creation date from video files
- Updates EXIF dates to match filename dates (harmonization)
- Adds EXIF metadata to files missing date information

### File Copy Service
- Determines destination path based on extracted date and media type
- Creates directory structure as needed
- Handles duplicate files silently
- Preserves original file attributes

### Sync Orchestrator
- Coordinates all services in the correct sequence
- Implements date resolution priority rules
- Collects operation statistics
- Handles errors gracefully without stopping the entire operation

## Data Flow

```
User Input (Compose UI)
           ↓
   SyncViewModel
           ↓
   ┌─────────────────────┐
   ↓                     ↓
File Scanner      Date Extractor
   ↓                     ↓
   └────────┬────────────┘
            ↓
    EXIF Metadata Service
            ↓
       File Copy Service
            ↓
       File System
```

## Date Resolution Priority

The application determines file dates using this priority order:

1. **Filename Date** (Primary Source)
   - Extracted using pattern matching on filename
   - When present, EXIF metadata is harmonized to match
   - Used for all media types

2. **EXIF Metadata** (Secondary - Images Only)
   - Used only when filename contains no date
   - Not applicable to videos

3. **Filesystem Timestamp** (Last Resort)
   - File creation or modification time
   - Used only when no other date source is available

4. **Undated Handling**
   - Files with no date source are skipped or moved to configured folder

## WhatsApp Detection

Files are identified as WhatsApp files when the filename contains WA after the date portion. These files are placed in a separate WhatsApp subfolder within Photos or Videos.

## Configuration Approach

Configuration is managed through a modern UI interface:
- **Configuration Screen**: Allows users to set paths, Valkey connectivity, and sync strategies.
- **Real-time Updates**: Settings are applied immediately to the Sync engine.

The application operates in two modes:
- **Dry-run mode**: Preview changes without making them (default)
- **Execute mode**: Perform actual file operations
