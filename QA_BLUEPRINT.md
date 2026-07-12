# 🛡️ QA Blueprint: CLI Regression Hardening
**Project:** `local-photo-sync`
**Target:** `scripts/cli-regression.sh`
**Version:** 1.0 (Hardening Phase)

## 1. Executive Summary
This blueprint defines the strategy for transitioning the `cli-regression.sh` suite from a basic validation tool to a high-integrity hardening suite. The goal is to ensure the CLI tool is resilient against environmental failures, data corruption, and extreme edge cases while maintaining the existing baseline performance and functionality.

## 2. Core Hardening Standards

### 2.1 File Integrity Verification (Checksumming)
To move beyond simple file-count checks, all "Success" criteria for file transfers must be backed by cryptographic checksums.

**Methodology:**
- **Algorithm:** SHA-256 (`sha256sum` or `shasum -a 256`).
- **Verification Flow:**
  1. Pre-calculate SHA-256 of the source file.
  2. Execute the sync operation.
  3. Post-calculate SHA-256 of the resulting destination file.
  4. Assert `source_hash == destination_hash`.
- **Scope:** This must be applied to all standard syncs, special character tests, and performance baselines.

### 2.2 Execution Telemetry (Timing)
Every test case must be timed to detect performance regressions (e.g., a change in the state-lookup algorithm that increases O(n) complexity).

**Methodology:**
- **Precision:** Nanosecond resolution using `date +%s%N`.
- **Calculation:** 
  `Duration = (End_Time_NS - Start_Time_NS) / 1,000,000,000`
- **Logging:** Results must be recorded in `regression.log` and `regression-reporter.log` in the format: `[TEST] <ID> | <RESULT> | Duration: <X.XXX>s`.
- **Thresholds:** Establish a "Baseline" (as seen in TC-11) where any increase > 20% over the baseline triggers a `WARNING` status.

## 3. Proposed High-Impact Edge Cases (Hardening Wave 2)

The following test cases are to be implemented to harden the system against "Real World" failures.

| ID | Scenario | Implementation Strategy | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **TC-12** | **Source File Corruption** | Overwrite random bytes in a source image mid-sync or use a corrupted header. | Tool should either detect hash mismatch or skip the file with a logged error, without crashing. |
| **TC-13** | **Destination Disk Full** | Use a mounted loopback device with a fixed small size (e.g., 10MB) to trigger `ENOSPC`. | Graceful exit with a "Disk Full" error; no partial/corrupted files left in destination. |
| **TC-14** | **Valkey State Outage** | Use `iptables` or stop the Valkey service mid-sync. | Automatic fallback to **STATELESS MODE**; sync continues without state persistence. |
| **TC-15** | **Interrupted Sync Recovery** | Trigger `kill -9` on the Java process during a large transfer. | On restart, tool must resume from the last successful file (verified via state store). |
| **TC-16** | **Deep Path Nesting** | Create a directory structure exceeding 255 characters. | Handle `FileNotFoundException` or `PathTooLongException` gracefully without recursive loops. |
| **TC-17** | **Concurrent Sync Conflicts** | Launch two identical sync commands simultaneously on the same source/dest. | Lock mechanism prevents data corruption; second process waits or exits with a "Lock Held" error. |
| **TC-18** | **Read-Only Source** | `chmod 444` the entire source directory. | Sync completes successfully; tool does not attempt to write metadata/logs to the source. |

## 4. Regression Safeguards (Non-Breaking)

To ensure stability, the following "Baseline" tests must remain untouched and maintain a 100% pass rate. Any change to these tests requires explicit architectural approval.

- **TC-01 (Standard Sync):** Ensures basic copy functionality.
- **TC-02 (Dry Run):** Ensures `--dryRun` prevents all write operations.
- **TC-03 (Skip Undated):** Ensures `--skipUndated` filters files without EXIF dates.
- **TC-04 (State Persistence):** Ensures subsequent runs skip already-synced files.

## 5. Certification Criteria
A build is considered **VANGUARD CERTIFIED** only if:
1. All TC-01 through TC-11 (Baseline) pass.
2. All new Hardening Wave 2 tests (TC-12 to TC-18) pass or fail gracefully (no crashes).
3. No `FATAL` or `VANGUARD-FATAL` errors appear in `regression.log`.
4. Performance durations remain within 1.2x of the established baseline.
