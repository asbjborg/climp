#!/usr/bin/env bash
# Convert MP3 files in assets/climp/sounds/ to OGG (Vorbis).
# Usage: Drop MP3s into src/main/resources/assets/climp/sounds/, then run:
#   ./scripts/mp3_to_ogg.sh
#
# Requires sox (recommended) or ffmpeg with libvorbis:
#   brew install sox

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOUNDS_DIR="$(cd "$SCRIPT_DIR/../src/main/resources/assets/climp/sounds" && pwd)"

use_sox() {
  command -v sox &>/dev/null
}

use_ffmpeg() {
  command -v ffmpeg &>/dev/null && ffmpeg -encoders 2>/dev/null | grep -q 'libvorbis'
}

convert_with_sox() {
  local mp3="$1" ogg="$2"
  sox "$mp3" -C 5 "$ogg"
}

convert_with_ffmpeg() {
  local mp3="$1" ogg="$2"
  ffmpeg -y -i "$mp3" -c:a libvorbis -q:a 5 "$ogg" -nostdin -loglevel warning
}

if use_sox; then
  CONVERT=convert_with_sox
elif use_ffmpeg; then
  CONVERT=convert_with_ffmpeg
else
  echo "Error: Need sox or ffmpeg (with libvorbis) to convert MP3 -> OGG."
  echo "Recommended: brew install sox"
  echo "Alternative: brew install ffmpeg  (must include libvorbis)"
  exit 1
fi

count=0
for mp3 in "$SOUNDS_DIR"/*.mp3; do
  [ -f "$mp3" ] || continue
  base="${mp3%.mp3}"
  ogg="${base}.ogg"
  echo "Converting: $(basename "$mp3") -> $(basename "$ogg")"
  $CONVERT "$mp3" "$ogg"
  ((count++)) || true
done

if [ "$count" -eq 0 ]; then
  echo "No MP3 files found in $SOUNDS_DIR"
  echo "Add .mp3 files there and run this script again."
  exit 0
fi

echo "Done. Converted $count file(s)."
