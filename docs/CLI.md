# CLI Reference

## Command Purpose

Organize photos and videos from a source directory into a structured destination by extracting dates from filenames and metadata.

## Core Options

### Source and Destination
- **Source path**: Directory containing the media files to organize
- **Destination path**: Target directory for organized files

### Dry-Run vs Execute
- **Dry-run mode**: Preview what would be copied without making any changes (default)
- **Execute mode**: Perform actual file copy operations

### Undated Files
- **Folder for undated files**: Files without dates can be moved to a designated folder
- **Skip undated files**: Option to skip files without dates instead of moving them

### Logging
- **Console logging**: Colored output for different log levels
- **File logging**: Optional log file output
- **Log levels**: Configurable verbosity from debug to error

## Usage Examples

**Preview changes before making them**
See what would happen without modifying any files.

**Execute the actual sync**
Copy files to their organized destination based on dates.

**Handle files without dates**
Configure whether undated files are collected in a folder or skipped.

**Enable verbose logging**
View detailed information about what the application is doing.

## Output

After each sync operation, a summary is displayed showing:
- Number of files copied
- Number of files skipped (duplicates or undated, depending on configuration)
- Number of errors encountered

## Starting the Application

The application starts with an interactive shell prompt where commands can be entered. Use the `help` command to see available commands and `exit` to quit.
