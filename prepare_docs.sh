#!/bin/bash
set -e  # Exit immediately if any command exits with a non-zero status

# Run Maven spotless to apply formatting
echo "[SCRIPT] ..Running: mvn spotless:apply.."
mvn spotless:apply

# Run Maven clean and test
echo "[SCRIPT] ..Running: mvn clean test.."
mvn clean test

# Run Maven asciidoctor to process AsciiDoc files
echo "[SCRIPT] ..Running: mvn asciidoctor:process-asciidoc.."
mvn asciidoctor:process-asciidoc

echo "[SCRIPT] [SUCCESS] ..All Maven tasks completed successfully!.."

# Execute the copy_index.sh script if it exists
if [ -f "./copy_index.sh" ]; then
    echo "[SCRIPT] ..Running copy_index.sh.."
    ./copy_index.sh
    echo "[SCRIPT] [SUCCESS] ..copy_index.sh executed successfully!.."
else
    echo "[SCRIPT] [FAIL] ..Error: copy_index.sh script not found!.."
    exit 1
fi

echo "[SCRIPT] [SUCCESS] ..All tasks completed successfully!.."
