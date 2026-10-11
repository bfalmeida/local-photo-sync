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
    - **Path traversal protection** via `PathSanitizer` — validates all file paths against directory escape attempts.
- **CLI Interface**: Powered by Spring Shell with structured command arguments.

---

## 🚀 Quick Start

### Requirements
- **Java 21**
- **Maven 3.9+**
- **Valkey/Redis** (optional, for state persistence and resume capability)

### Build and Run

```bash
# 1. Build the project
mvn clean package

# 2. Run the sync command directly (no interactive shell needed)
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar sync <source> <destination> [options]
```

The `sync` command is the primary entry point. Use `--help` to see all available options:

```bash
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar sync --help
```

---

## 🛠 Usage

### Basic Command Syntax

```
sync <source> <destination> [options]
```

**Arguments:**
- `source` (required) — Directory containing the media files to organize
- `destination` (required) — Target directory for organized files

### Common Scenarios

#### 1. Previewing a Sync (Dry Run)
By default, the app runs in dry-run mode. It shows exactly where files *would* go without actually moving them.

```bash
sync /Users/bruno/Pictures/Import /Users/bruno/Pictures/Organized
```

#### 2. Executing the Actual Sync
Add the `--execute` flag to perform the real file operations.

```bash
sync /Users/bruno/Pictures/Import /Users/bruno/Pictures/Organized --execute
```

#### 3. Handling Undated Files
If some files don't have a date in the filename or EXIF, specify a folder to collect them in:

```bash
sync /src /dest --undated-folder "Review_Needed" --execute
```

Alternatively, use `--skip-undated` to simply ignore files that cannot be dated:

```bash
sync /src /dest --skip-undated --execute
```

#### 4. Valkey State Persistence (Resume Capability)
Enable Valkey to track processed files and resume interrupted syncs:

```bash
sync /src /dest --execute --use-valkey
```

If a sync was interrupted, resume it with the same source/destination:

```bash
sync /src /dest --execute --use-valkey --resume-session
```

To clear previous state and start fresh:

```bash
sync /src /dest --execute --use-valkey --clear-state
```

#### 5. Customizing Logs
Need more detail for debugging? Change the log level on the fly:

```bash
sync /src /dest --execute --log-level DEBUG
```

Save logs to a file:

```bash
sync /src /dest --execute --log-file /var/log/photo-sync.log
```

#### 6. Combined Example
Full-featured sync with Valkey persistence, custom undated folder, and debug logging:

```bash
sync /media/import /media/organized \
  --execute \
  --use-valkey \
  --undated-folder "To_Review" \
  --log-level INFO \
  --log-file sync.log
```

---

### Complete Options Reference

| Option | Description | Default |
|--------|-------------|---------|
| `<source>` | Source directory containing media files | *(required, positional)* |
| `<destination>` | Destination directory for organized files | *(required, positional)* |
| `--execute` | Perform actual file copy operations | `false` (dry-run) |
| `--dry-run` | Preview changes without executing | `true` (implied when `--execute` not set) |
| `--undated-folder` | Folder name for files without date metadata | `undated` |
| `--skip-undated` | Skip files without dates instead of moving them | `false` |
| `--clear-state` | Reset the Valkey sync state before starting | `false` |
| `--use-valkey` | Enable Valkey state persistence | `false` |
| `--resume-session` | Resume existing session instead of creating new one | `false` |
| `--log-level` | Logging verbosity (`DEBUG`, `INFO`, `WARN`, `ERROR`) | `INFO` |
| `--log-file` | Path to a file for saving logs | `null` (console only) |

**Important constraints:**
- `--resume-session` and `--clear-state` cannot be used together.
- `--use-valkey` requires a running Valkey/Redis instance (configured via `valkey.host`, `valkey.port`, `valkey.password` in `application.yml` or environment variables).
- Dry-run is the default; files are never moved without `--execute`.

---

## 📂 Destination Structure

Files are organized as:

```
<destination>/
├── <year>/
│   ├── <month>/
│   │   ├── Photos/
│   │   │   └── [WhatsApp/]
│   │   └── Videos/
│   │       └── [WhatsApp/]
└── undated/                    # or custom --undated-folder name
    ├── Photos/
    └── Videos/
```

- **Photos** vs **Videos** separated by media type (based on file extension).
- **WhatsApp** files detected by filename pattern (`WA` after date) placed in `WhatsApp/` subfolder.
- **Undated** files go to the `undated` folder (customizable) unless `--skip-undated` is set.

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

---

## 📚 Documentation

For deep dives into the system, check the `docs/` directory:

- [Architecture](docs/ARCHITECTURE.md) — System design and service responsibilities.
- [CLI Reference](docs/CLI.md) — Detailed command usage.
- [Destination Structure](docs/DESTINATION_STRUCTURE.md) — How your files are organized.
- [Testing Guidelines](docs/TESTING.md) — Testing strategy and coverage.
- [Task List](docs/TASKS.md) — Full implementation roadmap.
- [Valkey Integration](docs/VALKEY_INTEGRATION.md) — State persistence and resume capability.

---

## ⚙️ Configuration

Configuration is managed via `src/main/resources/application.yml` or environment variables:

| Property | Default | Description |
|----------|---------|-------------|
| `valkey.host` | `127.0.0.1` | Valkey/Redis hostname |
| `valkey.port` | `6379` | Valkey/Redis port |
| `valkey.timeout` | `2000` | Connection timeout (ms) |
| `valkey.password` | *(empty)* | Authentication password (if enabled) |

Environment variables take precedence (e.g., `VALKEY_HOST`, `VALKEY_PASSWORD`).

---

## 🔒 Security

- **Path traversal protection**: All user-provided paths are validated via `PathSanitizer` to prevent directory escape attacks.
- **No hardcoded secrets**: Credentials loaded exclusively from configuration/environment.
- **HIDP compliant**: `@PreDestroy` cleanup, no `flushDb` side-effects, session-scoped hash keys.

---

## 📄 License

MIT