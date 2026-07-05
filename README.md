# Local Photo Sync - Vanguard Edition

A professional-grade synchronization tool designed to organize massive libraries of photos and videos by extracting dates from filenames and EXIF metadata.

## 🌟 Core Capabilities

- **Vanguard View (GUI)**: A high-performance Swing interface with a real-time dashboard and a "Heartbeat" system monitor.
- **Date-Driven Organization**: Automatically sorts files into a professional `YYYY/MM/` directory structure.
- **Intelligent Classification**: 
    - Separates **Photos** and **Videos** into distinct folders.
    - Detects and organizes **WhatsApp** media into dedicated subfolders.
- **Robust Metadata Extraction**: 
    - Primary: Filename patterns.
    - Fallback: EXIF metadata.
- **Enterprise-Grade Architecture**: Built with **Clean Architecture** (Hexagonal) to ensure total decoupling between the UI, Business Logic, and Persistence.
- **Persistence Engine**: Powered by **Valkey (Redis)** to track processed files across sessions and prevent duplicates.
- **Zero-Defect Foundation**: High-coverage test suite (JUnit 5/Mockito) ensuring absolute stability.

## 🚀 Quick Start

### Requirements
- **Java 21 (LTS)**
- **Maven 3.9+**
- **Valkey/Redis Instance** (Default: `192.168.0.132:6379`)

### Build and Run
```bash
# 1. Build the project
mvn clean package

# 2. Launch the application (starts the GUI by default)
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar
```

### CLI Mode
To launch the interactive Spring Shell instead of the GUI:
```bash
java -jar target/local-photo-sync-1.0.0-SNAPSHOT.jar --cli
```

## ⚙️ Configuration

The system is configured via `src/main/resources/application.yml`.

```yaml
valkey:
  host: 192.168.0.132
  port: 6379
  password: your_password

sync:
  threads: 4 # Number of concurrent copy operations
```

## 🛠 Architectural Blueprint

The system follows a strict decoupled flow:
**User Input** $\to$ `MainWindow` $\to$ `SyncController` $\to$ `SyncService` $\to$ `SyncStateRepository` $\to$ `Valkey`

Telemetry is handled asynchronously via a **SyncEventBus** (Observer Pattern), allowing the UI to react to progress and system health without blocking the engine.

## 📚 Documentation

For deep dives into the system, check the `docs/` directory:
- [Architecture](docs/ARCHITECTURE.md) - The la-Purity blueprint.
- [Valkey Integration](docs/VALKEY_INTEGRATION.md) - Persistence details.
- [CLI Reference](docs/CLI.md) - Detailed command usage.
- [Destination Structure](docs/DESTINATION_STRUCTURE.md) - How your files are organized.
- [Testing Guidelines](docs/TESTING.md) - TDD and coverage strategy.

## 📄 License
MIT
