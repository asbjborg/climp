#!/usr/bin/env python3
"""Generate voice lines via ElevenLabs and sync mod definitions from voicelines.json.

Usage:
  1. Copy .env.example to .env and set ELEVENLABS_API_KEY + ELEVENLABS_VOICE_ID
  2. Run: python3 scripts/generate_va.py
  3. Optional: python3 scripts/generate_va.py --dry-run

Behavior:
  - Pick category -> pick line -> generate preview -> accept or retry
  - Save accepted line as .ogg in assets/climp/sounds/
  - Keep timestamped backup when replacing existing .ogg
  - Regenerate ClimpSpeechLibrary, ClimpSoundEvents, and sounds.json from docs/va/voicelines.json
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from collections import OrderedDict
from datetime import datetime
from pathlib import Path

# Project paths
SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
VOICELINES_JSON = PROJECT_ROOT / "docs/va/voicelines.json"
SOUNDS_DIR = PROJECT_ROOT / "src/main/resources/assets/climp/sounds"
SOUNDS_JSON = PROJECT_ROOT / "src/main/resources/assets/climp/sounds.json"
LIBRARY_JAVA = PROJECT_ROOT / "src/main/java/com/asbjborg/climp/speech/ClimpSpeechLibrary.java"
SOUND_EVENTS_JAVA = PROJECT_ROOT / "src/main/java/com/asbjborg/climp/sound/ClimpSoundEvents.java"

# category_key -> (display_name, ClimpSpeechType, required id prefix)
CATEGORIES = OrderedDict(
    [
        ("idlelines", ("idle", "IDLE", "climp_idle")),
        ("hitlines", ("hit", "HIT", "climp_hit")),
        ("taskstartlines", ("task start", "TASK_START", "climp_task_start")),
        ("taskcompletelines", ("task complete", "TASK_COMPLETE", "climp_task_complete")),
        ("taskfailedunreachable", ("task failed (unreachable)", "TASK_FAILED_UNREACHABLE", "climp_task_failed_unreachable")),
        ("taskfailedtargetremoved", ("task failed (target removed)", "TASK_FAILED_TARGET_REMOVED", "climp_task_failed_target_removed")),
    ]
)

SOUND_ID_PATTERN = re.compile(r"^climp_[a-z0-9_]+_[0-9]+$")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate Climp voice lines with ElevenLabs.")
    parser.add_argument("--dry-run", action="store_true", help="Preview and validate only; do not write files.")
    return parser.parse_args()


def load_env() -> None:
    env_path = PROJECT_ROOT / ".env"
    if not env_path.exists():
        example = PROJECT_ROOT / ".env.example"
        print("Error: .env not found.")
        if example.exists():
            print("  Run: cp .env.example .env")
        print("  Then edit .env and add your ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID")
        sys.exit(1)

    for raw in env_path.read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        os.environ[key.strip()] = value.strip().strip('"').strip("'")


def load_voicelines() -> dict:
    try:
        return json.loads(VOICELINES_JSON.read_text())
    except FileNotFoundError:
        print(f"Error: missing {VOICELINES_JSON}")
        sys.exit(1)
    except json.JSONDecodeError as exc:
        print(f"Error: invalid JSON in {VOICELINES_JSON}: {exc}")
        sys.exit(1)


def validate_voicelines(data: dict) -> None:
    seen_ids: set[str] = set()
    errors: list[str] = []

    for key, (_, _, required_prefix) in CATEGORIES.items():
        lines = data.get(key, [])
        if not isinstance(lines, list):
            errors.append(f"{key}: expected list")
            continue

        for idx, line in enumerate(lines, 1):
            if not isinstance(line, dict):
                errors.append(f"{key}[{idx}]: expected object")
                continue
            for field in ("id", "textline", "voiceline"):
                if field not in line or not isinstance(line[field], str) or not line[field].strip():
                    errors.append(f"{key}[{idx}]: missing/invalid '{field}'")

            sound_id = line.get("id", "")
            if isinstance(sound_id, str):
                if not SOUND_ID_PATTERN.match(sound_id):
                    errors.append(f"{key}[{idx}]: invalid id '{sound_id}' (expected climp_*_<number>)")
                if not sound_id.startswith(required_prefix + "_"):
                    errors.append(f"{key}[{idx}]: id '{sound_id}' must start with '{required_prefix}_'")
                if sound_id in seen_ids:
                    errors.append(f"{key}[{idx}]: duplicate id '{sound_id}'")
                seen_ids.add(sound_id)

    if errors:
        print("Validation errors in voicelines.json:")
        for err in errors:
            print(" -", err)
        sys.exit(1)


def pick_category(data: dict) -> tuple[str, list[dict]]:
    print("\nCategories:")
    cats = [(key, CATEGORIES[key][0], data.get(key, [])) for key in CATEGORIES if data.get(key)]
    if not cats:
        print("No categories with lines found.")
        sys.exit(1)

    for i, (_, display, lines) in enumerate(cats, 1):
        print(f"  {i}. {display} ({len(lines)} lines)")

    while True:
        try:
            n = int(input("Pick category (number): ").strip())
            if 1 <= n <= len(cats):
                return cats[n - 1][0], cats[n - 1][2]
        except ValueError:
            pass
        print("Invalid. Try again.")


def pick_line(lines: list[dict]) -> dict:
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
    body = json.dumps({"text": text, "model_id": "eleven_v3"}).encode("utf-8")
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
    except urllib.error.HTTPError as exc:
        payload = exc.read().decode("utf-8", errors="replace") if exc.fp else ""
        print(f"ElevenLabs API error {exc.code}: {payload}")
        raise
    except urllib.error.URLError as exc:
        print(f"Request failed: {exc.reason}")
        raise


def play_audio(path: Path) -> None:
    if sys.platform == "darwin":
        subprocess.run(["afplay", str(path)], check=True)
    else:
        print("Play the file manually:", path)
        input("Press Enter when done listening...")


def convert_mp3_to_ogg(mp3: Path, ogg: Path) -> None:
    if not shutil.which("sox"):
        print("Error: sox is required for mp3 -> ogg conversion. Install: brew install sox")
        sys.exit(1)
    subprocess.run(["sox", str(mp3), "-C", "5", str(ogg)], check=True)


def backup_if_exists(ogg_path: Path) -> None:
    if not ogg_path.exists():
        return
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    backup = ogg_path.with_suffix(f".ogg.backup.{stamp}")
    shutil.copy2(ogg_path, backup)
    print(f"Backed up existing to {backup.name}")


def escape_java(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"')


def iter_entries(data: dict) -> list[tuple[str, str, str, str]]:
    """Return list of (category_key, enum_name, textline, sound_id) in canonical order."""
    entries: list[tuple[str, str, str, str]] = []
    for category_key, (_, enum_name, _) in CATEGORIES.items():
        for item in data.get(category_key, []):
            entries.append((category_key, enum_name, item["textline"], item["id"]))
    return entries


def build_library_content(data: dict) -> str:
    blocks: list[str] = []
    for i, (category_key, (_, enum_name, _)) in enumerate(CATEGORIES.items()):
        lines = data.get(category_key, [])
        if lines:
            line_defs = ",\n".join(
                f'                    new Line("{escape_java(item["textline"])}", "{item["id"]}")'
                for item in lines
            )
            block = f"            ClimpSpeechType.{enum_name}, List.of(\n{line_defs})"
        else:
            block = f"            ClimpSpeechType.{enum_name}, List.of()"
        if i < len(CATEGORIES) - 1:
            block += ","
        blocks.append(block)

    map_body = "\n".join(blocks)
    return f"""package com.asbjborg.climp.speech;

import java.util.List;
import java.util.Map;

import net.minecraft.util.RandomSource;

public final class ClimpSpeechLibrary {{
    /** Single source of truth: each line defines its text and the sound file name (e.g. climp_idle_1.ogg). */
    public record Line(String text, String soundId) {{}}

    private static final Map<ClimpSpeechType, List<Line>> LINES = Map.of(
{map_body});

    private ClimpSpeechLibrary() {{
    }}

    /**
     * Picks a random line for the given speech type. Never returns the excluded soundId if another
     * option exists, so the same line is not repeated twice in a row.
     */
    public static Line randomLine(ClimpSpeechType type, RandomSource random, String excludeSoundId) {{
        List<Line> lines = LINES.get(type);
        if (lines == null || lines.isEmpty()) {{
            return new Line("...", "climp_idle_1");
        }}
        List<Line> candidates = excludeSoundId != null && lines.size() > 1
                ? lines.stream().filter(l -> !l.soundId().equals(excludeSoundId)).toList()
                : lines;
        return candidates.get(random.nextInt(candidates.size()));
    }}
}}
"""


def build_sound_events_content(data: dict) -> str:
    sections: list[str] = []
    map_entries: list[str] = []

    for category_key, (_, enum_name, _) in CATEGORIES.items():
        items = data.get(category_key, [])
        if not items:
            continue

        section_lines = [f"    // {enum_name}"]
        for item in items:
            var_name = item["id"].upper().replace("-", "_")
            section_lines.append(
                f'    public static final DeferredHolder<SoundEvent, SoundEvent> {var_name} = reg("{item["id"]}");'
            )
            map_entries.append(f'            Map.entry("{item["id"]}", {var_name})')
        sections.append("\n".join(section_lines))

    entries_joined = ",\n".join(map_entries)
    sections_joined = "\n\n".join(sections)

    return f"""package com.asbjborg.climp.sound;

import java.util.Map;

import com.asbjborg.climp.ClimpMod;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events for Climp voice lines. One event per line (e.g., climp_idle_1, climp_idle_2).
 */
public final class ClimpSoundEvents {{
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT, ClimpMod.MODID);

{sections_joined}

    private static final Map<String, Holder<SoundEvent>> BY_SOUND_ID = Map.ofEntries(
{entries_joined});

    private ClimpSoundEvents() {{
    }}

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {{
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ClimpMod.MODID, name)));
    }}

    /** Returns the sound event for the given soundId (e.g. "climp_idle_1"). Uses climp_idle_1 as fallback if unknown. */
    public static Holder<SoundEvent> get(String soundId) {{
        Holder<SoundEvent> holder = BY_SOUND_ID.get(soundId);
        return holder != null ? holder : CLIMP_IDLE_1;
    }}
}}
"""


def build_sounds_json_content(data: dict) -> str:
    ordered: OrderedDict[str, dict] = OrderedDict()
    for category_key in CATEGORIES:
        for item in data.get(category_key, []):
            sound_id = item["id"]
            ordered[sound_id] = {"sounds": [f"climp:{sound_id}"]}
    return json.dumps(ordered, indent=2) + "\n"


def write_atomic(path: Path, content: str) -> bool:
    existing = path.read_text() if path.exists() else None
    if existing == content:
        return False
    tmp_path = path.with_suffix(path.suffix + ".tmp")
    tmp_path.write_text(content)
    tmp_path.replace(path)
    return True


def sync_definition_files(data: dict, dry_run: bool) -> None:
    desired = {
        LIBRARY_JAVA: build_library_content(data),
        SOUND_EVENTS_JAVA: build_sound_events_content(data),
        SOUNDS_JSON: build_sounds_json_content(data),
    }

    changed = []
    for path, content in desired.items():
        if path.exists() and path.read_text() != content:
            changed.append(path)

    if not changed:
        print("Definition files already in sync.")
        return

    if dry_run:
        print("Dry-run: would update definition files:")
        for path in changed:
            print(f" - {path}")
        return

    for path in changed:
        write_atomic(path, desired[path])
    print("Synced definitions:")
    for path in changed:
        print(f" - {path}")


def main() -> None:
    args = parse_args()
    load_env()

    api_key = os.environ.get("ELEVENLABS_API_KEY")
    voice_id = os.environ.get("ELEVENLABS_VOICE_ID")
    if not api_key or not voice_id:
        print("Error: .env must contain ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID")
        sys.exit(1)

    data = load_voicelines()
    validate_voicelines(data)

    category_key, lines = pick_category(data)
    line_data = pick_line(lines)

    sound_id = line_data["id"]
    voiceline = line_data["voiceline"]

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

    if args.dry_run:
        print(f"Dry-run: would save {sound_id}.ogg")
        sync_definition_files(data, dry_run=True)
        print("Done (dry-run).")
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

    sync_definition_files(data, dry_run=False)
    print("Done.")


if __name__ == "__main__":
    main()
