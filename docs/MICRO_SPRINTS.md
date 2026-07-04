# Micro-Sprint Backlog: Swing UI Migration

This document decomposes the Technical Design Document (TDD) for the Swing UI migration into atomic micro-sprints. Each task is designed to be completed within a single subagent session (< 600s).

## Sprint Overview
The migration moves `local-photo-sync` from a Spring Shell CLI to a native Java Swing UI, preserving core business logic while introducing asynchronous execution and real-time progress reporting.

---

## Micro-Sprint Backlog

| Task ID | Goal | Acceptance Criteria | Dependencies | Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **MS-001** | **Main Window Layout** | `PhotoSyncWindow` created; contains Header, PathSelection, Options, Log, and Button panels as per TDD hierarchy. | None | Low |
| **MS-002** | **Source Path Selection** | "Browse" button for Source opens `JFileChooser` (Directories only); selected path updates `sourceField`. | MS-001 | Low |
| **MS-003** | **Dest Path Selection** | "Browse" button for Destination opens `JFileChooser` (Directories only); selected path updates `destField`. | MS-001 | Low |
| **MS-004** | **Options Panel Wiring** | All checkboxes (DryRun, Execute, SkipUndated, ClearState) and LogLevel combo box are functional and state-trackable. | MS-001 | Low |
| **MS-005** | **Sync Service Adapter** | `SyncServiceAdapter` component implemented to wrap `SyncService` calls in `CompletableFuture` with log consumers. | None | Medium |
| **MS-006** | **Background Execution** | `SwingWorker` implemented in `PhotoSyncWindow` to call `SyncServiceAdapter` without blocking the EDT. | MS-004, MS-005 | Medium |
| **MS-007** | **Real-time UI Logging** | `TextAreaAppender` implemented; Logback events are routed to the `logArea` JTextArea on the EDT. | MS-001 | Medium |
| **MS-008** | **Progress Bar Integration** | Progress bar switches from Indeterminate (Scanning) to Determinate (Copying) based on `SyncService` status. | MS-006 | Medium |
| **MS-009** | **Input Validation** | Start button triggers validation: checks for null/empty paths and directory existence; shows `JOptionPane` errors. | MS-002, MS-003 | Low |
| **MS-010** | **Confirmation Dialogs** | "Execute" mode triggers a confirmation dialog before starting file operations. | MS-004, MS-009 | Low |
| **MS-011** | **UI Entry Point** | `PhotosyncApplication` modified to launch `PhotoSyncWindow` on startup if not in headless mode. | MS-001 | Medium |
| **MS-012** | **CLI Mode Preservation** | Added `--cli` flag support to `main` method to bypass UI and launch traditional ShellRunner. | MS-011 | Low |
| **MS-013** | **Dependency Cleanup** | `spring-shell-starter` removed from `pom.xml`; unused Shell components deleted. | MS-012 | Low |
| **MS-014** | **Dry-Run Verification** | End-to-end test: Start sync with DryRun enabled; verify files are listed in logs but not copied. | MS-006, MS-007 | Medium |
| **MS-015** | **Execution Verification** | End-to-end test: Start sync with Execute enabled; verify files are physically moved to destination. | MS-006, MS-007 | Medium |
| **MS-016** | **Cancel/Stop Logic** | "Stop" button triggers `SwingWorker.cancel(true)`; verify `SyncService` loop terminates gracefully. | MS-006 | Medium |

---

## Execution Notes
- **Threading:** Always verify that `SwingUtilities.invokeLater` is used for UI updates from background threads.
- **Validation:** Path validation must occur *before* the `SwingWorker` is instantiated.
- **Verification:** Every MS should be verified by the Auditor using the criteria defined in Section 9 of the TDD.
