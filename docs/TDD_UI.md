# Technical Design Document: local-photo-sync User Interface

## 1. Framework Selection
**Selected Framework:** Compose for Desktop (Jetpack Compose for Desktop)

### Justification
Compose for Desktop is chosen over JavaFX for the following reasons:
- **Declarative UI:** Modern declarative approach reduces boilerplate and allows for faster iteration of UI components compared to the imperative nature of JavaFX.
- **Kotlin-First:** Seamless integration with the existing Kotlin codebase, leveraging coroutines for asynchronous operations.
- **Performance:** High-performance rendering via Skia, ensuring smooth animations and responsive layouts.
- **GraalVM Compatibility:** Strong support for native compilation via GraalVM, aligning with the project's goal of distributing a standalone native binary.
- **Consistency:** Shared mental model with Android development, simplifying potential future expansions or cross-platform considerations.

## 2. Architecture
The UI will follow a **Model-View-ViewModel (MVVM)** pattern to decouple the presentation layer from the business logic provided by the Spring Boot services.

### Interaction Flow
- **View (Compose):** Responsible for rendering the UI and capturing user events. It observes state changes from the ViewModel.
- **ViewModel:** Acts as a bridge. It interacts with the Spring Boot services and exposes state as `StateFlow` or `MutableState` to the View.
- **Service Layer (Spring Boot):** The existing `SyncService`, `ValkeyStateService`, and other core logic.

### Non-Blocking UI Thread
To prevent UI freezes during long-running sync operations or large directory scans:
- **Kotlin Coroutines:** All service calls will be executed within `viewModelScope` using `Dispatchers.IO`.
- **Asynchronous State Updates:** The `SyncService` will emit progress updates via a `Flow`, which the ViewModel collects and pushes to the UI state, ensuring the Main thread remains responsive.

## 3. UI Map
The interface will be organized into a single-window application with a sidebar or tabbed navigation.

### 3.1 Path Selection
- **Source Directory Picker:** A native folder selection dialog to define the source of the photos.
- **Destination Directory Picker:** A native folder selection dialog to define the target sync location.
- **Validation:** Immediate feedback if paths are invalid, read-only, or if the destination is a sub-folder of the source.

### 3.2 Configuration
- **Valkey Settings:** Input fields for Valkey host, port, and password. A "Test Connection" button to verify availability.
- **Performance Tuning:** Sliders/Input fields for thread count and batch size to optimize throughput based on hardware.
- **Sync Strategy:** Dropdown to select sync modes (e.g., Mirror, Merge).

### 3.3 Execution Control
- **Dry-Run Mode:** A toggle/checkbox to simulate the sync process without modifying files.
- **Execute Button:** Primary action button to trigger the sync. Disabled until paths and critical configurations are validated.
- **Cancel/Pause:** Ability to gracefully stop a running sync process.

### 3.4 Real-time Progress Dashboard
- **Global Progress Bar:** Overall percentage of completion.
- **File-by-File Log:** A scrollable list showing currently processed files, their status (Synced, Skipped, Error), and transfer speed.
- **Statistics Panel:** Total files scanned, total size transferred, and time elapsed/remaining.

## 4. Native Integration
The goal is to produce a single, high-performance native binary using **GraalVM**.

### Compilation Plan
1. **GraalVM JDK:** Use the GraalVM JDK to compile the project.
2. **Native Image Tool:** Utilize the `native-image` tool to compile the JVM bytecode into a native executable.
3. **Reflection Configuration:** Since Spring Boot and Compose use reflection, detailed `reflect-config.json` files will be generated (using the GraalVM tracing agent) to ensure all necessary classes are available at runtime.
4. **Packaging:** Use the Compose Gradle plugin's native distribution tasks to package the binary with required native libraries (e.g., Skia).

## 5. Failure Vectors & Mitigations

| Potential Issue | Proposed Solution |
| :--- | :--- |
| **UI Freeze during directory scan** | Move directory walking to a background coroutine (`Dispatchers.IO`) and update the UI via a `Flow` of discovered files. |
| **Memory exhaustion with millions of files** | Implement pagination or virtualized lists (LazyColumn in Compose) for the file-by-file log to avoid loading all entries into RAM. |
| **Spring Context overhead in Native** | Use Spring Boot's AOT (Ahead-of-Time) processing to minimize startup time and memory footprint in the native binary. |
| **Native Library Mismatches** | Ensure the build environment strictly matches the target OS architecture and utilize CI/CD runners for each target platform (Windows, macOS, Linux). |
| **Valkey Connection Timeout** | Implement an asynchronous heartbeat/ping mechanism with a UI timeout indicator to notify the user if the state store becomes unreachable. |
