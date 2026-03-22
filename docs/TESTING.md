# Testing Guidelines

## Testing Categories

### Unit Tests
Individual services tested in isolation with dependencies mocked. Focus on business logic, date extraction, path generation, and error handling.

### Integration Tests
Complete workflow tested end-to-end, verifying that all services work together correctly and that files are organized as expected.

## Coverage Target

**Minimum: 80% line coverage** across all services.

## Test Scenarios

### Date Extraction
- All supported filename date formats
- Edge cases (malformed dates, missing components)
- WhatsApp file detection
- Media type detection (Photo vs Video)

### EXIF Handling
- Reading EXIF dates from images
- Updating EXIF dates for harmonization
- Adding EXIF to images without metadata
- Handling files without EXIF gracefully

### File Operations
- Correct destination path generation based on date
- Duplicate file handling (skip silently)
- Folder structure creation as needed
- File attribute preservation

### File Discovery
- Finding media files in directories
- Filtering by supported extensions
- Recursive directory scanning
- Handling empty directories

### Workflow Orchestration
- Full synchronization process
- Date resolution priority
- EXIF harmonization
- Statistics collection and reporting

## Test Data

### Images
- Multiple filename date format variations
- Files with matching EXIF and filename dates
- Files with mismatched EXIF dates (for harmonization testing)
- Files without any EXIF metadata

### Videos
- Video files with dates in filename
- WhatsApp video files

### Edge Cases
- Files without parseable dates
- Duplicate filenames
- Files in nested directories
- Special characters in filenames

## Best Practices

1. **Independent tests**: Each test runs independently without depending on other tests
2. **Descriptive names**: Test names clearly describe what is being verified
3. **Single responsibility**: One assertion focus per test for clear failure diagnosis
4. **Arrange-Act-Assert**: Follow the AAA pattern consistently
5. **Edge cases**: Test boundary conditions, empty values, and null inputs
6. **Performance**: Keep unit tests fast by avoiding slow operations
7. **Mock external dependencies**: Isolate units from file system and other services

## Test Data Approach

For file operations, use temporary directories that are automatically cleaned up after tests. This allows testing actual file operations without polluting the file system.
