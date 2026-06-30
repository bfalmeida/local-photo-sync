# local-photo-sync - Project Roadmap (v1.0.0-STABLE → v2.0.0)

This document outlines the strategic evolution of `local-photo-sync` from its current stable release toward a more robust, production-ready version 2.0.0.

## Strategic Vision
The goal for v2.0.0 is to transition the tool from a "functional utility" to a "production-grade synchronization engine." This involves moving beyond basic file movement toward industrial-strength reliability, deep observability, and broader accessibility.

---

## Phase 7: Interface & Native Leap (Immediate Priority)
*Focus: Transforming the tool from a CLI utility to a user-friendly, standalone application.*

### 7.1 User Interface (UI) Implementation
- **UI Framework**: Implement a modern desktop interface (e.g., Compose for Desktop or JavaFX).
- **Core Features**: 
    - Visual path selection for source and destination.
    - Real-time progress monitoring with visual bars.
    - Integrated configuration panel for Valkey and threading.
    - One-click 'Dry-Run' and 'Execute' buttons.
- **UX Flow**: Ensure a seamless transition from setup to execution.

### 7.2 CLI Experience Polish
- **Picocli Integration**: Upgrade the CLI to provide professional-grade command parsing, colors, and auto-completion.
- **Consistency**: Ensure the UI and CLI share the same underlying service layer for identical behavior.

### 7.3 GraalVM Native Compilation
- **Native Binary**: Configure GraalVM to compile the application (UI + Engine) into a single, standalone native binary.
- **Optimization**: Eliminate JVM startup lag and reduce memory footprint for a 'instant-on' experience.


---

## Phase 8: Performance & Scaling
*Focus: Handling libraries of millions of photos efficiently.*

- **Parallel Processing**: Implement a worker-pool pattern for hashing and copying files.
- **Valkey Pipelining**: Optimize state updates to reduce network round-trips to the Valkey instance.
- **Incremental Scanning**: Use filesystem watchers (WatchService) to detect new photos in real-time rather than full directory scans.

---

## Phase 9: Intelligence & Metadata Enrichment
*Focus: Moving beyond dates to semantic organization.*

- **Advanced Deduplication**: Implement perceptual hashing (pHash) to find visually similar photos, even if metadata differs.
- **AI-Powered Tagging**: Integration with local ML models (e.g., via ONNX) for basic category tagging (Nature, People, Documents).
- **Custom Folder Templates**: Allow users to define their own destination patterns (e.g., `{year}/{event}/{month}`).

---

## Phase 10: Ecosystem Expansion
*Focus: Breaking the "local-only" barrier.*

- **Cloud Storage Connectors**: Support for S3, Google Drive, and Azure Blob Storage as source/destination.
- **Cross-Platform Daemon**: A background service that keeps folders synchronized automatically.
- **Plugin Architecture**: Allow third-party developers to add new `DateExtractor` or `FileTransport` implementations.

---

## Versioning Milestones

| Version | Milestone | Key Deliverable |
|---------|-----------|-----------------|
| **v1.0.0-STABLE** | Baseline | SHA-256 Sync, Valkey State, Java 17 |
| **v1.1.0** | User-Centric | UI, Native Binary, CLI Polish |
| **v1.5.0** | Scaled | Parallel Sync, Real-time Watchers |
| **v2.0.0** | Intelligent | Cloud Integration, Semantic Tagging, Plugin API |
