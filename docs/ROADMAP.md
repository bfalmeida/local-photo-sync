# Project Roadmap: local-photo-sync

## Current Status: v1.1.0-BETA (Stable)

### Phase 1-6: Core Engine [COMPLETED]
- [x] SHA-256 Content-Addressable Sync.
- [x] Date Extraction Pipeline (Filename $\rightarrow$ EXIF $\rightarrow$ Filesystem).
- [x] Valkey State Persistence.
- [x] Atomic "Copy $\rightarrow$ Verify $\rightarrow$ Delete" Transport.

### Phase 7: Interface & Native Leap [COMPLETED]
- [x] **UI Framework**: Implemented via Compose for Desktop.
- [x] **Architecture**: MVVM pattern with non-blocking Coroutines.
- [x] **User Experience**: Configuration panel, native path pickers, and a real-time Progress Dashboard.
- [x] **Unified Core**: Complete migration from Java to Kotlin for zero-conflict build stability.
- [x] **Build System**: Migration to Gradle for framework compatibility.
- [ ] **Native Binary**: GraalVM native compilation (Pending environment toolchain setup).

---

## Future Objectives

### Phase 8: Performance & Scaling
- **Parallel Processing**: Implement a worker-pool pattern for hashing and copying files.
- **Valkey Pipelining**: Optimize state updates to reduce network round-trips to the Valkey instance.
- **Incremental Scanning**: Use filesystem watchers (WatchService) to detect new photos in real-time.

### Phase 9: Intelligence & Metadata Enrichment
- **Advanced Deduplication**: Implement perceptual hashing (pHash) to find visually similar photos.
- **AI-Powered Tagging**: Integration with local ML models for basic category tagging.
- **Custom Folder Templates**: Allow users to define their own destination patterns.

### Phase 10: Ecosystem Expansion
- **Cross-Platform Packagers**: Automating the creation of `.deb`, `.msi`, and `.dmg` installers.
- **Cloud Integration**: Optional mirroring to S3 or Google Drive.
