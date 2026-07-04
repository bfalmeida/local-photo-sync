# Technical Design Document: Swing Desktop UI Migration

## 1. Executive Summary

This document outlines the technical design for migrating the `local-photo-sync` application from a Spring Shell CLI to a native Java Swing Desktop UI. The migration preserves all existing business logic while providing a graphical interface for users to configure and execute photo synchronization operations.

## 2. Current Architecture Analysis

### 2.1 Project Structure
- **Java Version:** 17
- **Build System:** Maven
- **Framework:** Spring Boot 3.2.5 with Spring Shell 3.2.4
- **Source Path:** `/root/local-photo-sync/src/main/java/com/github/bfalmeida/photosync/`

### 2.2 Existing Components

| Component | Purpose | Spring Annotation |
|-----------|---------|-------------------|
| `PhotosyncApplication` | Spring Boot entry point | `@SpringBootApplication` |
| `SyncCommand` | CLI command handler | `@ShellComponent` |
| `Main` | Shell exit command | `@ShellComponent` |
| `SyncService` | Core synchronization logic | `@Service` |
| `MediaFileScanner` | Media file discovery | `@Service` |
| `ExifMetadataService` | EXIF metadata extraction/manipulation | `@Component` |
| `FileCopyService` | Atomic file copying with verification | `@Service` |
| `HashingService` | File content hashing | `@Component` |
| `ValkeyStateService` | Redis state management | `@Service` |

### 2.3 Sync Command Parameters (to be replicated in UI)

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `source` | String | required | Source directory containing photos |
| `destination` | String | required | Destination directory for organized photos |
| `dryRun` | boolean | true | Preview mode without actual file operations |
| `execute` | boolean | false | Execute actual copy operations |
| `undatedFolder` | String | "undated" | Folder name for files without date metadata |
| `skipUndated` | boolean | false | Skip files without date metadata |
| `clearState` | boolean | false | Reset Valkey sync state |
| `logLevel` | String | INFO | Logging level (DEBUG, INFO, WARN, ERROR) |
| `logFile` | String | null | Optional log file path |

## 3. Proposed Architecture

### 3.1 Swing Component Hierarchy

```
JFrame (PhotoSyncWindow) - Main application window
├── JPanel (mainPanel) - BorderLayout container
│   ├── JPanel (headerPanel) - Title and app info
│   │   └── JLabel - Application title
│   │
│   ├── JPanel (pathSelectionPanel) - Source/destination selection
│   │   ├── JPanel (sourcePanel) - Source directory selection
│   │   │   ├── JLabel "Source:"
│   │   │   ├── JTextField (sourceField) - Path display
│   │   │   └── JButton (sourceBrowseButton) - Open JFileChooser
│   │   │
│   │   └── JPanel (destPanel) - Destination directory selection
│   │       ├── JLabel "Destination:"
│   │       ├── JTextField (destField) - Path display
│   │       └── JButton (destBrowseButton) - Open JFileChooser
│   │
│   ├── JPanel (optionsPanel) - Sync options configuration
│   │   ├── JCheckBox (dryRunCheckBox) - Preview mode
│   │   ├── JCheckBox (executeCheckBox) - Execute mode
│   │   ├── JTextField (undatedFolderField) - Undated folder name
│   │   ├── JCheckBox (skipUndatedCheckBox) - Skip undated files
│   │   ├── JCheckBox (clearStateCheckBox) - Clear Valkey state
│   │   └── JComboBox (logLevelCombo) - DEBUG/INFO/WARN/ERROR
│   │
│   ├── JProgressBar (progressBar) - Visual progress indicator
│   │
│   ├── JSplitPane (splitPane) - Log output area
│   │   └── JScrollPane (logScrollPane)
│   │       └── JTextArea (logArea) - Read-only log display
│   │
│   └── JPanel (buttonPanel) - Action buttons
│       ├── JButton (startButton) - Start synchronization
│       ├── JButton (stopButton) - Stop operation
│       └── JButton (exitButton) - Exit application
```

### 3.2 Event Thread Management

**Critical Requirement:** All long-running operations (file scanning, copying) must execute off the EDT (Event Dispatch Thread) to prevent UI freezing.

#### 3.2.1 SwingWorker Pattern

```java
SwingWorker<SyncStatistics, String> syncWorker = new SwingWorker<>() {
    @Override
    protected SyncStatistics doInBackground() throws Exception {
        // Execute sync in background thread
        return syncService.synchronize(...);
    }
    
    @Override
    protected void process(List<String> chunks) {
        // Update log output on EDT
        chunks.forEach(logArea::append);
    }
    
    @Override
    protected void done() {
        // UI updates on EDT when complete
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
};
```

#### 3.2.2 Progress Reporting

- Progress bar updates via `publish()/process()` during file scanning
- Real-time log output via `publish()/process()` for each processed file
- Cancel support via `isCancelled()` check in worker loop
- Indeterminate mode during scanning (unknown file count), determinate during copy

### 3.3 Service Wiring

```
PhotoSyncWindow (UI Layer)
    │
    ├── Depends on: SyncService, MediaFileScanner (via ApplicationContext)
    │
    └── Delegates to: [Business logic unchanged]

SyncService (Unchanged)
    ├── MediaFileScanner
    ├── FilenameDateExtractor
    ├── ExifMetadataService
    ├── FileCopyService
    ├── HashingService
    └── ValkeyStateService
```

#### 3.3.1 Spring Integration

```java
// Bootstrapped from existing Spring context
public class PhotoSyncWindow extends JFrame {
    private final SyncService syncService;
    private final MediaFileScanner mediaFileScanner;
    
    // Wire via constructor injection from Spring context
    public PhotoSyncWindow(ApplicationContext context) {
        this.syncService = context.getBean(SyncService.class);
        this.mediaFileScanner = context.getBean(MediaFileScanner.class);
    }
}
```

### 3.4 UI Service Adapter

Create `ui.SyncServiceAdapter` to bridge SwingWorker with SyncService:

```java
@Component
public class SyncServiceAdapter {
    private final SyncService syncService;
    
    public CompletableFuture<SyncStatistics> synchronizeAsync(
            Path source, Path destination, boolean execute, 
            String undatedFolder, boolean skipUndated, 
            boolean clearState, String sessionId,
            Consumer<String> logConsumer) {
        // Wrap sync in CompletableFuture for async execution
    }
}
```

## 4. Failure Vectors and Error Handling

### 4.1 Path Validation Failures

| Failure | Detection | UI Response |
|---------|-----------|-------------|
| Source path null/empty | Validate on Start click | Show `JOptionPane.ERROR_MESSAGE`: "Source directory is required" |
| Source path doesn't exist | `File.exists()` check | Show error dialog with path |
| Source not a directory | `File.isDirectory()` check | Show error dialog |
| Destination exists but not directory | `File.exists() && !isDirectory()` | Show error dialog |
| Destination parent unreadable | File permission check | Show error: "Cannot create destination directory" |

### 4.2 Execution Failures

| Failure | Detection Point | UI Response |
|---------|-----------------|-------------|
| Permission denied on read | `Files.copy()` throws `AccessDeniedException` | Log error, increment error counter, show summary dialog |
| Permission denied on write | `Files.createDirectories()` fails | Same as above |
| Disk full during copy | `IOException: No space left` | Stop operation, show error dialog |
| Redis connection failure | ValkeyStateService operations fail | Continue with warning, disable state tracking |
| File not found during scan | `Files.walk()` yields missing file | Log warning, skip file, continue |

### 4.3 UI Error Dialogs

```java
// Standardized error dialog
private void showError(String title, String message) {
    JOptionPane.showMessageDialog(
        this, 
        message, 
        title, 
        JOptionPane.ERROR_MESSAGE
    );
}

// Confirmation dialog for destructive operations
private boolean confirmExecute(String source, String dest) {
    return JOptionPane.showConfirmDialog(
        this,
        "Copy files from " + source + " to " + dest + "?",
        "Confirm Execute",
        JOptionPane.YES_NO_OPTION
    ) == JOptionPane.YES_OPTION;
}
```

## 5. File Copy Behavior

### 5.1 JFileChooser Integration

```java
private void selectSourceDirectory() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select Source Directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        sourceField.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}
```

### 5.2 Directory Chooser Features

- Show hidden files option
- Recent directory history via Preferences API
- Start in user home directory

## 6. Logging Integration

### 6.1 Log Appender for UI

Create `ui.TextAreaAppender` extending `AppenderBase<ILoggingEvent>`:

```java
public class TextAreaAppender extends AppenderBase<ILoggingEvent> {
    private final JTextArea textArea;
    
    @Override
    protected void append(ILoggingEvent eventObject) {
        String formatted = layout.toByteArray(eventObject);
        SwingUtilities.invokeLater(() -> {
            textArea.append(formatted);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
```

### 6.2 Progress Bar Updates

- **Scanning Phase:** Indeterminate progress bar
- **Copy Phase:** Determinate progress with file count total
- **Completed:** Green bar, 100%
- **Error:** Red bar, error count summary

## 7. Configuration Changes

### 7.1 Maven Dependencies

Remove:
```xml
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter</artifactId>
    <version>${spring-shell.version}</version>
</dependency>
```

Add:
```xml
<!-- No additional dependencies required - Swing is in JDK -->
```

### 7.2 Application Properties

Add to `application.yml`:
```yaml
spring:
  main:
    headless: false  # Allow UI operations
    
app:
  window:
    width: 800
    height: 600
    title: "Local Photo Sync"
```

## 8. Entry Point Changes

### 8.1 Modified Main Bootstrap

```java
@SpringBootApplication
public class PhotosyncApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(PhotosyncApplication.class, args);
        
        // Check if running in headless mode
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                PhotoSyncWindow window = new PhotoSyncWindow(context);
                window.setVisible(true);
            });
        }
    }
}
```

### 8.2 Command Line Mode Preservation

Support `--cli` flag for backward compatibility:
```java
// If --cli present, launch ShellRunner instead of Swing
if (Arrays.asList(args).contains("--cli")) {
    // Traditional CLI mode
} else {
    // Swing mode
}
```

## 9. Verification Criteria for Auditor

### 9.1 Functional Verification

| Criterion | Test Method | Expected Result |
|-----------|-------------|-----------------|
| Source selection launches JFileChooser | Click source browse button | Directory chooser dialog opens |
| Destination selection launches JFileChooser | Click dest browse button | Directory chooser dialog opens |
| Start without paths shows error dialog | Click Start with empty fields | Error dialog: "Source and Destination directories are required" |
| Start with invalid source path shows error | Enter non-existent path | Error dialog: "Source path does not exist" |
| Dry-run mode previews without copying | Run without Execute checked | Files listed in log, no actual copy |
| Execute mode copies files | Run with Execute checked | Files copied to destination structure |
| Progress bar updates during sync | Large source directory | Bar moves, log updates in real-time |
| Stop button cancels operation | Click Stop during sync | Operation terminates gracefully |
| Exit closes application | Click Exit button | JVM exits cleanly |

### 9.2 Technical Verification

| Criterion | Verification Method |
|-----------|-------------------|
| EDT not blocked during sync | Use Swing Thread Monitor or log thread dumps during operation |
| No console errors in UI mode | Run with `java -jar app.jar`, verify clean exit |
| Memory release on exit | JConsole or VisualVM shows no lingering threads after Exit |
| All existing tests pass | `mvn test` - 100% pass rate |
| No Spring Shell dependency at runtime | `mvn dependency:tree` shows no spring-shell in UI mode |

### 9.3 Integration Points

- **MediaFileScanner:** Unchanged - streams files via `Files.walk()`
- **SyncService:** Unchanged - execute in background via SwingWorker
- **FileCopyService:** Unchanged - atomic copy with hash verification
- **ValkeyStateService:** Unchanged - Redis state management
- **ExifMetadataService:** Unchanged - metadata extraction/manipulation

## 10. Implementation Sequence

1. **Phase 1:** Create `PhotoSyncWindow` with component layout
2. **Phase 2:** Implement path selection with JFileChooser
3. **Phase 3:** Wire options panel to sync parameters
4. **Phase 4:** Implement SwingWorker for background sync
5. **Phase 5:** Add progress reporting and log display
6. **Phase 6:** Implement error dialogs and validation
7. **Phase 7:** Update entry point and remove Shell dependency
8. **Phase 8:** Test dry-run and execute modes

## 11. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| UI freezes during large sync | Thorough background threading with proper SwingWorker implementation |
| Memory leak from log output | Limit log area buffer or add clear button |
| Headless environment failure | Graceful fallback or clear error message |
| Redis connection blocking UI | Async connection with timeout, disable state features if unavailable |
| Cross-platform look-and-feel | Use system L&F: `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel())` |

---

**Document Version:** 1.0  
**Date:** 2026-07-04  
**Status:** Ready for Implementation