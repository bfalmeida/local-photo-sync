#!/bin/bash
set -e

PROJECT_ROOT="$(pwd)"
JAR_FILE=$(find target -name "*.jar" ! -name "*sources.jar" ! -name "*javadoc.jar" | head -n 1)
DATASET_ROOT="$PROJECT_ROOT/test-dataset"
SRC_DIR="$DATASET_ROOT/source"
DEST_DIR="$DATASET_ROOT/destination"
UNDATED_DIR="$DATASET_ROOT/undated"

echo "---------------------------------------------------"
echo " l-Surgical l-Regression Test: CLI Sync"
echo "---------------------------------------------------"

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No executable JAR found in target/. Run 'mvn package' first."
    exit 1
fi

echo "[1/4] Preparing Terrain..."
mkdir -p "$SRC_DIR" "$DEST_DIR" "$UNDATED_DIR"
rm -rf "$DEST_DIR"/*

# Create a test image if none exists
if [ -z "$(ls -A $SRC_DIR)" ]; then
    echo "No test images found. Generating dummy asset..."
    dd if=/dev/urandom of="$SRC_DIR/test_image.jpg" bs=1k count=10 2>/dev/null
fi

echo "[2/4] Executing CLI Sync..."
# We use --cli to trigger CLI mode and sync command with required options
java -jar "$JAR_FILE" --cli sync \
    --source "$SRC_DIR" \
    --destination "$DEST_DIR" \
    --execute \
    --undatedFolder "$UNDATED_DIR"

echo "[3/4] Verifying Results..."
FILE_COUNT_SRC=$(ls -1 "$SRC_DIR" | wc -l)
FILE_COUNT_DEST=$(ls -1 "$DEST_DIR" | wc -l)

echo "Source files: $FILE_COUNT_SRC"
echo "Destination files: $FILE_COUNT_DEST"

if [ "$FILE_COUNT_SRC" -eq "$FILE_COUNT_DEST" ] && [ "$FILE_COUNT_SRC" -gt 0 ]; then
    echo "---------------------------------------------------"
    echo "RESULT: PASS 🟢"
    echo "---------------------------------------------------"
    exit 0
else
    echo "---------------------------------------------------"
    echo "RESULT: FAIL 🔴"
    echo "Expected $FILE_COUNT_SRC files in destination, found $FILE_COUNT_DEST."
    echo "---------------------------------------------------"
    exit 1
fi
