#!/usr/bin/env bash
# Convert MP3 files in assets/climp/sounds/ to OGG (Vorbis).
# Usage:
#   ./scripts/mp3_to_ogg.sh           # Convert only MP3s with no existing .ogg
#   ./scripts/mp3_to_ogg.sh --all     # Regenerate all .ogg files from MP3s
#
# Requires sox (recommended) or ffmpeg with libvorbis:
#   brew install sox

set -e

REGENERATE_ALL=false
if [[ "${1:-}" == "--all" || "${1:-}" == "-a" ]]; then
  REGENERATE_ALL=true
fi

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
  if [[ -f "$ogg" && "$REGENERATE_ALL" != "true" ]]; then
    echo "Skipping (exists): $(basename "$ogg")"
    continue
  fi
  echo "Converting: $(basename "$mp3") -> $(basename "$ogg")"
  $CONVERT "$mp3" "$ogg"
  ((count++)) || true
done

if [ "$count" -eq 0 ]; then
  echo "No conversions performed."
  echo "Add new .mp3 files to $SOUNDS_DIR or use --all to regenerate existing .ogg files."
  exit 0
fi

echo "Done. Converted $count file(s)."
