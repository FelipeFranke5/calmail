#!/bin/bash
set -e  # Exit immediately if a command exits with a non-zero status

# Define source and destination file paths
SRC="target/generated-docs/index.html"
DEST="src/main/resources/static/index.html"

# Check if the source file exists
if [ ! -f "$SRC" ]; then
  echo "Error: Source file '$SRC' does not exist."
  exit 1
fi

# Create destination directory if it doesn't exist
mkdir -p "$(dirname "$DEST")"

# Copy the file to the destination
cp "$SRC" "$DEST"

echo "File copied from '$SRC' to '$DEST' successfully!"
