#!/usr/bin/env bash
# Build the Climp mod JAR and copy it to dist/ for easy drop‑into‑mods-folder install.
set -e
cd "$(dirname "$0")/.."

./gradlew build --quiet

VERSION=$(grep mod_version gradle.properties | cut -d= -f2)
JAR="build/libs/climp-${VERSION}.jar"
DIST_DIR="dist"
DIST_JAR="${DIST_DIR}/climp-${VERSION}.jar"

mkdir -p "$DIST_DIR"
cp "$JAR" "$DIST_JAR"

echo "Built: $DIST_JAR"
echo ""
echo "  Drop this file into your Minecraft instance's mods folder."
echo "  (NeoForge 1.21.1 required.)"
echo ""
