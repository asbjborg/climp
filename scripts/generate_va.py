#!/usr/bin/env python3
"""Generate voice lines via ElevenLabs and optionally add them to the mod.

Usage:
  1. Copy .env.example to .env and add your ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID.
  2. Run: python3 scripts/generate_va.py

Flow: pick category -> pick line -> generates audio -> plays it -> ask use? -> if yes:
  - backs up existing .ogg if replacing
  - converts mp3 to ogg (sox), saves to sounds folder
  - if new line: updates ClimpSpeechLibrary, ClimpSoundEvents, sounds.json
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from pathlib import Path

# Project paths
SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
VOICELINES_JSON = PROJECT_ROOT / "docs/va/voicelines.json"
SOUNDS_DIR = PROJECT_ROOT / "src/main/resources/assets/climp/sounds"
SOUNDS_JSON = PROJECT_ROOT / "src/main/resources/assets/climp/sounds.json"
LIBRARY_JAVA = PROJECT_ROOT / "src/main/java/com/asbjborg/climp/speech/ClimpSpeechLibrary.java"
SOUND_EVENTS_JAVA = PROJECT_ROOT / "src/main/java/com/asbjborg/climp/sound/ClimpSoundEvents.java"

# JSON category key -> (display name, ClimpSpeechType enum value)
CATEGORIES = {
    "idlelines": ("idle", "IDLE"),
    "hitlines": ("hit", "HIT"),
    "taskstartlines": ("task start", "TASK_START"),
    "taskcompletelines": ("task complete", "TASK_COMPLETE"),
    "taskfailedunreachable": ("task failed (unreachable)", "TASK_FAILED_UNREACHABLE"),
    "taskfailedtargetremoved": ("task failed (target removed)", "TASK_FAILED_TARGET_REMOVED"),
}


def load_env() -> None:
    env_path = PROJECT_ROOT / ".env"
    if not env_path.exists():
        example = PROJECT_ROOT / ".env.example"
        print("Error: .env not found.")
        if example.exists():
            print("  Run: cp .env.example .env")
        print("  Then edit .env and add your ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID")
        sys.exit(1)
    for line in env_path.read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, _, value = line.partition("=")
            os.environ[key.strip()] = value.strip().strip('"')


def load_voicelines() -> dict:
    data = json.loads(VOICELINES_JSON.read_text())
    return data


def pick_category(data: dict) -> tuple[str, list]:
    print("\nCategories:")
    cats = [(key, CATEGORIES[key][0], data[key]) for key in CATEGORIES if key in data and data[key]]
    for i, (key, display, lines) in enumerate(cats, 1):
        print(f"  {i}. {display} ({len(lines)} lines)")
    while True:
        try:
            n = int(input("Pick category (number): ").strip())
            if 1 <= n <= len(cats):
                return cats[n - 1][0], cats[n - 1][2]
        except ValueError:
            pass
        print("Invalid. Try again.")


def pick_line(lines: list) -> dict:
    print("\nLines:")
    for i, line in enumerate(lines, 1):
        print(f"  {i}. [{line['id']}] {line['textline']}")
    while True:
        try:
            n = int(input("Pick line (number): ").strip())
            if 1 <= n <= len(lines):
                return lines[n - 1]
        except ValueError:
            pass
        print("Invalid. Try again.")


def generate_audio(api_key: str, voice_id: str, text: str) -> bytes:
    url = f"https://api.elevenlabs.io/v1/text-to-speech/{voice_id}?output_format=mp3_44100_128"
    body = json.dumps({"text": text, "model_id": "eleven_v3"}).encode()
    req = urllib.request.Request(
        url,
        data=body,
        headers={
            "Content-Type": "application/json",
            "xi-api-key": api_key,
            "Accept": "audio/mpeg",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.read()
    except urllib.error.HTTPError as e:
        body = e.read().decode() if e.fp else ""
        print(f"ElevenLabs API error {e.code}: {body}")
        raise
    except urllib.error.URLError as e:
        print(f"Request failed: {e.reason}")
        raise


def play_audio(path: Path) -> None:
    if sys.platform == "darwin":
        subprocess.run(["afplay", str(path)], check=True)
    else:
        print("Play the file manually:", path)
        input("Press Enter when done listening...")


def convert_mp3_to_ogg(mp3: Path, ogg: Path) -> None:
    if shutil.which("sox"):
        subprocess.run(["sox", str(mp3), "-C", "5", str(ogg)], check=True)
    elif shutil.which("ffmpeg"):
        subprocess.run(
            ["ffmpeg", "-y", "-i", str(mp3), "-c:a", "libvorbis", "-q:a", "5", str(ogg)],
            check=True,
            capture_output=True,
        )
    else:
        print("Error: Need sox or ffmpeg to convert mp3 -> ogg. Install: brew install sox")
        sys.exit(1)


def backup_if_exists(ogg_path: Path) -> None:
    if ogg_path.exists():
        backup = ogg_path.with_suffix(".ogg.backup")
        shutil.copy2(ogg_path, backup)
        print(f"Backed up existing to {backup.name}")


def id_exists_in_java(sound_id: str) -> bool:
    content = LIBRARY_JAVA.read_text()
    return f'"{sound_id}"' in content


def add_to_library(category_key: str, textline: str, sound_id: str) -> None:
    _, enum_val = CATEGORIES[category_key]
    content = LIBRARY_JAVA.read_text()
    search = f"ClimpSpeechType.{enum_val}, List.of("
    start = content.find(search)
    if start == -1:
        print("Warning: Could not find insertion point in ClimpSpeechLibrary")
        return
    end = content.find("ClimpSpeechType.", start + len(search))
    if end == -1:
        end = len(content)
    block = content[start:end]
    idx = block.rfind('")),')
    if idx == -1:
        print("Warning: Could not find list end in ClimpSpeechLibrary")
        return
    suffix_after = block[idx + 4 :]
    escaped = textline.replace('\\', '\\\\').replace('"', '\\"')
    new_block = (
        block[:idx]
        + '"),\n                    new Line("'
        + escaped
        + '", "'
        + sound_id
        + '")),'
        + suffix_after
    )
    LIBRARY_JAVA.write_text(content[:start] + new_block + content[end:])


def add_to_sound_events(sound_id: str, speech_type: str) -> None:
    content = SOUND_EVENTS_JAVA.read_text()
    var_name = sound_id.upper().replace("-", "_")
    reg_line = f'    public static final DeferredHolder<SoundEvent, SoundEvent> {var_name} = reg("{sound_id}");'
    map_line = f'            Map.entry("{sound_id}", {var_name}),'

    # Find last reg in same family (e.g. climp_idle_5 for new climp_idle_6)
    parts = sound_id.split("_")
    num = int(parts[-1])
    prefix = "_".join(parts[:-1])
    last_id = f"{prefix}_{num - 1}"

    # Add reg line after last in family
    reg_idx = content.find(f'reg("{last_id}")')
    if reg_idx == -1:
        print("Warning: Could not find insertion point in ClimpSoundEvents for reg")
        return
    line_end = content.find("\n", reg_idx)
    content = content[: line_end + 1] + reg_line + "\n" + content[line_end + 1 :]

    # Add Map.entry after last in family (need comma before new entry)
    map_insert = content.find(f'Map.entry("{last_id}",')
    if map_insert != -1:
        paren_end = content.find(")", map_insert) + 1
        content = content[:paren_end] + ",\n            " + map_line + content[paren_end:]

    SOUND_EVENTS_JAVA.write_text(content)


def add_to_sounds_json(sound_id: str) -> None:
    data = json.loads(SOUNDS_JSON.read_text())
    data[sound_id] = {"sounds": [f"climp:{sound_id}"]}
    SOUNDS_JSON.write_text(json.dumps(data, indent=2) + "\n")


def main() -> None:
    load_env()
    api_key = os.environ.get("ELEVENLABS_API_KEY")
    voice_id = os.environ.get("ELEVENLABS_VOICE_ID")
    if not api_key or not voice_id:
        print("Error: .env must contain ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID")
        sys.exit(1)

    data = load_voicelines()
    category_key, lines = pick_category(data)
    line_data = pick_line(lines)

    sound_id = line_data["id"]
    voiceline = line_data["voiceline"]
    textline = line_data["textline"]

    while True:
        print(f"\nGenerating: {voiceline[:60]}...")
        try:
            audio = generate_audio(api_key, voice_id, voiceline)
        except Exception:
            sys.exit(1)

        with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as f:
            tmp_mp3 = Path(f.name)
            f.write(audio)

        print("Playing...")
        play_audio(tmp_mp3)

        use = input("\nUse this voice line? [y/N]: ").strip().lower()
        tmp_mp3.unlink(missing_ok=True)

        if use == "y":
            break
        try_again = input("Try again? [Y/n]: ").strip().lower()
        if try_again == "n":
            print("Skipped.")
            return

    SOUNDS_DIR.mkdir(parents=True, exist_ok=True)
    ogg_path = SOUNDS_DIR / f"{sound_id}.ogg"

    backup_if_exists(ogg_path)

    # ElevenLabs returns mp3; convert to ogg
    with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as f:
        tmp = Path(f.name)
        f.write(audio)
    convert_mp3_to_ogg(tmp, ogg_path)
    tmp.unlink(missing_ok=True)

    print(f"Saved: {ogg_path}")

    is_new = not id_exists_in_java(sound_id)
    if is_new:
        _, enum_val = CATEGORIES[category_key]
        add_to_library(category_key, textline, sound_id)
        add_to_sound_events(sound_id, enum_val)
        add_to_sounds_json(sound_id)
        print("Wired up Java (ClimpSpeechLibrary, ClimpSoundEvents, sounds.json)")
    else:
        print("Replaced existing; no Java changes needed.")

    print("Done.")


if __name__ == "__main__":
    main()
