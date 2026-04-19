# local-photo-sync

A command-line application to organize photos and videos by extracting dates from filenames and EXIF metadata.

Application being developed by AI.

## 🌟 Features

- **Date-Driven Organization**: Automatically sorts files into a `YYYY/MM/` directory structure.
- **Smart Detection**: 
    - Separates **Photos** and **Videos** into distinct folders.
    - Specifically detects and organizes **WhatsApp** media into dedicated subfolders.
- **Robust Date Extraction**: 
    - Uses filename patterns as the primary source of truth.
    - Falls back to **EXIF metadata** for images.
- **Safety First**:
    - **Dry-run mode** is enabled by default to let you preview changes before any files are moved.
- **Interactive CLI**: Powered by Spring Shell for an easy-to-use command experience.

## 🚀 Quick Start

### Requirements
- **Java 21**
- **Maven 3.9+**

### Build and Run
```bash
# 1. Build the project
mvn clean package

# 2. Launch the interactive shell
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar
```

Once the shell is running, you can use the `sync` command to organize your files.

---

## 🛠 Usage

The primary command is `sync`. Since it's an interactive shell, you just type the command and its options.

### Basic Command Syntax
`sync --source <path> --destination <path> [options]`

### Common Scenarios

#### 1. Previewing a Sync (Dry Run)
By default, the app runs in dry-run mode. It will show you exactly where files *would* go without actually moving them.
```bash
sync --source /Users/bruno/Pictures/Import --destination /Users/bruno/Pictures/Organized
```

#### 2. Executing the Actual Sync
When you are happy with the preview, add the `--execute` flag to perform the real file operations.
```bash
sync --source /Users/bruno/Pictures/Import --destination /Users/bruno/Pictures/Organized --execute
```

#### 3. Handling Undated Files
If some files don't have a date in the filename or EXIF, you can specify a folder to collect them in:
```bash
sync --source /src --destination /dest --undatedFolder "Review_Needed" --execute
```
*Alternatively, use `--skipUndated` to simply ignore files that cannot be dated.*

#### 4. Customizing Logs
Need more detail for debugging? Change the log level on the fly:
```bash
sync --source /src --destination /dest --logLevel DEBUG
```

### Summary of Options

| Option | Description | Default |
| :--- | :--- | :--- |
| `--source` | Directory containing the media files | (Required) |
| `--destination` | Target directory for organized files | (Required) |
| `--execute` | Perform actual file copy operations | `false` |
| `--undatedFolder` | Folder name for files without date metadata | `undated` |
| `--skipUndated` | Skip files without dates instead of moving them | `false` |
| `--logLevel` | Logging verbosity (`DEBUG`, `INFO`, `WARN`, `ERROR`) | `INFO` |
| `--logFile` | Path to a file for saving logs | `null` |

---

## 📈 Progress

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
- [x] TASK-012: Update README.md
- [ ] TASK-013: Final Integration Test

## 📚 Documentation

For deep dives into the system, check the `docs/` directory:
- [Architecture](docs/ARCHITECTURE.md) - System design and service responsibilities.
- [CLI Reference](docs/CLI.md) - Detailed command usage.
- [Destination Structure](docs/DESTINATION_STRUCTURE.md) - How your files are organized.
- [Testing Guidelines](docs/TESTING.md) - Testing strategy and coverage.
- [Task List](docs/TASKS.md) - Full implementation roadmap.

## 📄 License
MIT
