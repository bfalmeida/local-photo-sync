# local-photo-sync

A professional-grade desktop application to organize photos and videos by extracting dates from filenames and EXIF metadata.

Application being developed by AI.

## 🌟 Features

- **Date-Driven Organization**: Automatically sorts files into a `YYYY/MM/` directory structure.
- **Smart Detection**: 
    - Separates **Photos** and **Videos** into distinct folders.
    - Specifically detects and organizes **WhatsApp** media into dedicated subfolders.
- **Robust Date Extraction**: 
    - Uses filename patterns as the primary source of truth.
    - Falls back to **EXIF metadata** for images.
- **Professional UI**: A modern Compose for Desktop interface for effortless configuration and real-time monitoring.
- **Safety First**:
    - **Dry-run mode** allows you to preview exactly where files *would* go without actually moving them.
- **High Performance**: Asynchronous sync engine with multi-threaded processing and Valkey-backed state persistence.

## 🚀 Quick Start

### Requirements
- **Java 21**
- **Gradle 8.7+**

### Build and Run
```bash
# 1. Build the project
./gradlew build

# 2. Launch the Desktop Application
./gradlew run
```

Once the application launches, use the **Configuration Screen** to set your source and destination folders, test your Valkey connection, and execute the sync.

---

## 🛠 Usage Guide

The application is managed through a intuitive graphical interface.

### 1. Configuration
- **Directories**: Use the "Browse" buttons to select your source (where the photos are) and destination (where you want them organized) folders.
- **Valkey Settings**: Enter your Valkey host and password. Click **Test Connection** to verify connectivity.
- **Sync Strategy**:
    - **Dry Run**: When enabled, the app only simulates the sync.
    - **Undated Folder**: Specify the name of the folder for files that cannot be dated.
    - **Skip Undated**: When enabled, files without dates are ignored instead of being moved.
- **Performance**: Adjust the thread count and batch size based on your hardware.

### 2. Execution
Click **Execute Sync**. The app will switch to the **Progress Dashboard**, providing:
- **Real-time Stats**: Total files processed, copied, and skipped.
- **Global Progress**: A visual bar showing the percentage of completion.
- **Virtualized Log**: A live, color-coded stream of every file operation.

### 3. Monitoring
You can **Cancel** the sync at any time, and the app will gracefully stop and save the current state to Valkey.

---

## 📈 Progress

- [x] **Phase 1-6: Core Engine** (Hashing, Scanning, Date Extraction, Copy Logic) - **DONE**
- [x] **Phase 7: Interface Leap** (Compose UI, MVVM Architecture, Gradle Migration) - **DONE**
- [x] **Great Unification** (Unified Kotlin Core) - **DONE**

## 📚 Documentation

For deep dives into the system, check the `docs/` directory:
- [Architecture](docs/ARCHITECTURE.md) - System design and service responsibilities.
- [UI Design](docs/TDD_UI.md) - Technical design of the interface.
- [Roadmap](docs/ROADMAP.md) - Project evolution and future targets.
- [Task List](docs/TASKS.md) - Detailed implementation history.
- [Destination Structure](docs/DESTINATION_STRUCTURE.md) - How your files are organized.

## 📄 License
MIT
