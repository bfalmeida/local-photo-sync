#!/bin/bash
set -e

# Resolve project root relative to script location and normalize to absolute path
PROJECT_ROOT=$(readlink -f "$(dirname "$(readlink -f "$0")")/..")
JAR_FILE=$(find "$PROJECT_ROOT/target" -name "*.jar" ! -name "*sources.jar" ! -name "*javadoc.jar" | head -n 1)
DATASET_ROOT="$PROJECT_ROOT/test-dataset"
SRC_DIR="$DATASET_ROOT/source"
DEST_DIR="$DATASET_ROOT/destination"
UNDATED_DIR="$DATASET_ROOT/undated"

echo "---------------------------------------------------"
echo "🛡️  VANGUARD FULL-SPECTRUM REGRESSION SUITE"
echo "---------------------------------------------------"

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No executable JAR found."
    exit 1
fi

# Utility to reset destination
reset_dest() {
    rm -rf "$DEST_DIR"/*
}

# --- TEST CASE 01: STANDARD SYNC ---
echo "[TC-01] Standard Sync..."
reset_dest
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --undatedFolder "$UNDATED_DIR" > /dev/null
FILE_COUNT=$(find "$DEST_DIR" -type f | wc -l)
if [ "$FILE_COUNT" -gt 0 ]; then
    echo "  ✅ PASS: Files copied."
else
    echo "  ❌ FAIL: No files copied."
    exit 1
fi

# --- TEST CASE 02: DRY RUN ---
echo "[TC-02] Dry Run (Verify no-op)..."
reset_dest
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --dryRun true --undatedFolder "$UNDATED_DIR" > /dev/null
FILE_COUNT=$(find "$DEST_DIR" -type f | wc -l)
if [ "$FILE_COUNT" -eq 0 ]; then
    echo "  ✅ PASS: No files copied during dry-run."
else
    echo "  ❌ FAIL: Files copied during dry-run!"
    exit 1
fi

# --- TEST CASE 03: SKIP UNDATED ---
echo "[TC-03] Skip Undated..."
# Create a truly undated file (no date in name, random content)
TOUCH_FILE="$SRC_DIR/totally_undated.jpg"
dd if=/dev/urandom of="$TOUCH_FILE" bs=1k count=1 2>/dev/null

reset_dest
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --skipUndated true --undatedFolder "$UNDATED_DIR" > /dev/null
if [ ! -f "$DEST_DIR/undated/Photos/totally_undated.jpg" ] && [ ! -f "$DEST_DIR/undated/Videos/totally_undated.jpg" ]; then
    echo "  ✅ PASS: Undated file was skipped."
else
    echo "  ❌ FAIL: Undated file was copied despite skip flag."
    exit 1
fi

# --- TEST CASE 04: CLEAR STATE ---
echo "[TC-04] Clear State (Force Re-sync)..."
reset_dest
# First pass
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --undatedFolder "$UNDATED_DIR" > /dev/null
# Second pass (should be 0 copied due to state)
reset_dest
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --undatedFolder "$UNDATED_DIR" > /dev/null
if [ "$(find "$DEST_DIR" -type f | wc -l)" -ne 0 ]; then
    echo "  ❌ FAIL: State not working; files re-copied without clearState."
    exit 1
fi
# Third pass with clearState
reset_dest
java -jar "$JAR_FILE" --cli sync --source "$SRC_DIR" --destination "$DEST_DIR" --execute --clearState true --undatedFolder "$UNDATED_DIR" > /dev/null
if [ "$(find "$DEST_DIR" -type f | wc -l)" -gt 0 ]; then
    echo "  ✅ PASS: State cleared and files re-copied."
else
    echo "  ❌ FAIL: clearState did not trigger re-sync."
    exit 1
fi

echo "---------------------------------------------------"
echo "RESULT: FULL SUITE PASS 🟢"
echo "---------------------------------------------------"
