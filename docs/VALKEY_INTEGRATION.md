# Valkey Integration for State Management

## Overview
To support resume capabilities and avoid redundant processing of files in large synchronization tasks, `local-photo-sync` integrates Valkey (an open-source Redis-compatible data store). This integration allows the application to track which files have been successfully processed and maintain session statistics across restarts.

## Design Goals
- **Resume Capability**: Ability to start a sync process and resume from the last successfully processed file after an interruption.
- **Performance**: Use fast in-memory lookups to check if a file has already been synchronized.
- **Configurability**: Avoid hardcoded connection details; use property-based configuration.
- **At-Least-Once Processing**: Ensure that a file is marked as processed only after a successful copy operation.

## Data Model

### 1. Processed Files Set
**Key**: `sync:processed_files:{session_id}`
**Type**: `Set`
**Value**: Relative path of the media file from the source root.
**Usage**: 
- `SADD`: Mark a file as processed.
- `SISMEMBER`: Check if a file has already been synchronized.
- `SCARD`: Get the total number of processed files.

### 2. Session Metadata
**Key**: `sync:session:{session_id}`
**Type**: `Hash`
**Fields**:
- `source`: Absolute path to the source directory.
- `destination`: Absolute path to the destination directory.
- `start_time`: ISO-8601 timestamp of when the sync started.
- `status`: `IN_PROGRESS` | `COMPLETED` | `FAILED`.
- `last_processed_file`: Relative path of the last file successfully handled.

### 3. Session Statistics
**Key**: `sync:stats:{session_id}`
**Type**: `Hash`
**Fields**:
- `copied`: Integer count of successfully copied files.
- `skipped`: Integer count of skipped files.
- `errors`: Integer count of files that encountered errors.

## Configuration
Connection settings are managed via `application.yml` or environment variables.

| Property | Default | Description |
|----------|---------|-------------|
| `valkey.host` | `127.0.0.1` | Hostname of the Valkey server |
| `valkey.port` | `6379` | Port of the Valkey server |
| `valkey.timeout` | `2000` | Connection timeout in milliseconds |
| `valkey.password` | (empty) | Password for authentication (if enabled) |

## Workflow

### Starting a New Sync
1. Generate a unique `session_id` (e.g., UUID).
2. Create the `sync:session:{session_id}` hash with source, destination, and `IN_PROGRESS` status.
3. Initialize `sync:stats:{session_id}` with zeroed counts.

### Processing Files
For each file discovered by `MediaFileScanner`:
1. **Check State**: Call `SISMEMBER sync:processed_files:{session_id} <relative_path>`.
2. **Skip if Processed**: If true, increment `skipped` stats and continue to next file.
3. **Execute Sync**: Perform the date resolution and file copy.
4. **Update State**:
    - If `CopyResult.SUCCESS`: 
        - `SADD sync:processed_files:{session_id} <relative_path>`
        - `HSET sync:session:{session_id} last_processed_file <relative_path>`
        - Increment `copied` in `sync:stats:{session_id}`.
    - If `CopyResult.SKIPPED`:
        - Increment `skipped` in `sync:stats:{session_id}`.
    - If `CopyResult.ERROR`:
        - Increment `errors` in `sync:stats:{session_id}`.

### Resuming a Sync
1. User provides a `session_id` (or the app looks up the last `IN_PROGRESS` session).
2. Verify that the source and destination paths in `sync:session:{session_id}` still match the current request.
3. Continue scanning from the source, using the existing `sync:processed_files` set to skip already handled files.

### Completing a Sync
1. Update `sync:session:{session_id}` status to `COMPLETED`.
2. (Optional) Set an expiration (TTL) on the session keys to avoid permanent memory growth.
