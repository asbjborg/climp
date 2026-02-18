#!/usr/bin/env python3
"""Cut a Keep-a-Changelog release section from Unreleased.

Usage examples:
  python3 scripts/changelog_release.py --bump patch
  python3 scripts/changelog_release.py --bump minor
  python3 scripts/changelog_release.py --version 1.0.0
"""

from __future__ import annotations

import argparse
import datetime as dt
import re
import sys
from pathlib import Path


UNRELEASED_TEMPLATE = [
    "## [Unreleased]",
    "",
    "### Added",
    "- *(nothing yet)*",
    "",
    "### Changed",
    "- *(nothing yet)*",
    "",
    "### Fixed",
    "- *(nothing yet)*",
    "",
    "---",
    "",
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Create a new changelog release from Unreleased.")
    parser.add_argument("--file", default="CHANGELOG.md", help="Path to changelog file.")
    parser.add_argument(
        "--bump",
        choices=("major", "minor", "patch"),
        help="Semantic bump relative to latest released version.",
    )
    parser.add_argument("--version", help="Explicit release version (e.g. 0.3.0).")
    parser.add_argument("--date", help="Release date in YYYY-MM-DD format. Defaults to today.")
    parser.add_argument(
        "--allow-empty",
        action="store_true",
        help="Allow cutting a release even if Unreleased only contains placeholders.",
    )
    parser.add_argument("--dry-run", action="store_true", help="Print what would change without writing.")
    args = parser.parse_args()

    if bool(args.bump) == bool(args.version):
        parser.error("Provide exactly one of --bump or --version.")

    return args


def parse_semver(version: str) -> tuple[int, int, int]:
    match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)", version)
    if not match:
        raise ValueError(f"Invalid semantic version '{version}'. Expected format: X.Y.Z")
    return tuple(int(group) for group in match.groups())


def format_semver(parts: tuple[int, int, int]) -> str:
    return f"{parts[0]}.{parts[1]}.{parts[2]}"


def bump_semver(version: str, bump: str) -> str:
    major, minor, patch = parse_semver(version)
    if bump == "major":
        return format_semver((major + 1, 0, 0))
    if bump == "minor":
        return format_semver((major, minor + 1, 0))
    return format_semver((major, minor, patch + 1))


def find_section_indices(lines: list[str]) -> tuple[int, int]:
    unreleased_idx = next((i for i, line in enumerate(lines) if line.strip() == "## [Unreleased]"), -1)
    if unreleased_idx < 0:
        raise ValueError("Missing '## [Unreleased]' section.")

    next_section_idx = -1
    for i in range(unreleased_idx + 1, len(lines)):
        if re.match(r"^## \[", lines[i]):
            next_section_idx = i
            break

    if next_section_idx < 0:
        raise ValueError("Could not find first released section after 'Unreleased'.")

    return unreleased_idx, next_section_idx


def extract_latest_version(lines: list[str], first_release_idx: int) -> str:
    match = re.match(r"^## \[(\d+\.\d+\.\d+)\]", lines[first_release_idx].strip())
    if not match:
        raise ValueError("First release header after Unreleased is not a semantic version section.")
    return match.group(1)


def normalize_unreleased_body(raw_lines: list[str]) -> list[str]:
    body = raw_lines[:]
    while body and not body[0].strip():
        body.pop(0)
    while body and not body[-1].strip():
        body.pop()
    if body and body[-1].strip() == "---":
        body.pop()
    while body and not body[-1].strip():
        body.pop()
    return body


def has_substantive_entries(lines: list[str]) -> bool:
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("- ") and stripped != "- *(nothing yet)*":
            return True
    return False


def infer_release_url(lines: list[str], version: str) -> str:
    for line in lines:
        match = re.match(r"^\[\d+\.\d+\.\d+\]:\s*(https?://\S+)$", line.strip())
        if not match:
            continue
        existing_url = match.group(1)
        replaced = re.sub(r"/releases/tag/v\d+\.\d+\.\d+$", f"/releases/tag/v{version}", existing_url)
        if replaced != existing_url:
            return replaced

    for line in lines:
        match = re.match(r"^\[Unreleased\]:\s*(https?://\S+)$", line.strip())
        if not match:
            continue
        compare_url = match.group(1)
        compare_match = re.match(r"^(https?://[^ ]+)/compare/v?\d+\.\d+\.\d+\.\.\.HEAD$", compare_url)
        if compare_match:
            return f"{compare_match.group(1)}/releases/tag/v{version}"

    raise ValueError("Could not infer release URL. Add standard link refs to CHANGELOG first.")


def update_link_refs(lines: list[str], new_version: str) -> list[str]:
    updated = lines[:]

    unreleased_ref_idx = next((i for i, line in enumerate(updated) if line.startswith("[Unreleased]: ")), -1)
    if unreleased_ref_idx < 0:
        raise ValueError("Missing [Unreleased] link reference in changelog footer.")

    unreleased_ref = updated[unreleased_ref_idx]
    updated[unreleased_ref_idx] = re.sub(
        r"/compare/v?\d+\.\d+\.\d+\.\.\.HEAD",
        f"/compare/v{new_version}...HEAD",
        unreleased_ref,
    )

    version_ref = f"[{new_version}]: "
    has_version_ref = any(line.startswith(version_ref) for line in updated)
    if not has_version_ref:
        release_url = infer_release_url(updated, new_version)
        updated.insert(unreleased_ref_idx + 1, f"[{new_version}]: {release_url}")

    return updated


def main() -> int:
    args = parse_args()
    changelog_path = Path(args.file)
    if not changelog_path.exists():
        print(f"ERROR: File not found: {changelog_path}", file=sys.stderr)
        return 1

    original_text = changelog_path.read_text(encoding="utf-8")
    lines = original_text.splitlines()

    unreleased_idx, first_release_idx = find_section_indices(lines)
    latest_version = extract_latest_version(lines, first_release_idx)
    next_version = args.version or bump_semver(latest_version, args.bump)
    parse_semver(next_version)  # Validate explicit version.

    release_date = args.date or dt.date.today().isoformat()
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", release_date):
        print("ERROR: --date must use YYYY-MM-DD format.", file=sys.stderr)
        return 1

    unreleased_body = normalize_unreleased_body(lines[unreleased_idx + 1 : first_release_idx])
    if not args.allow_empty and not has_substantive_entries(unreleased_body):
        print(
            "ERROR: Unreleased has no substantive entries. Use --allow-empty to force a release cut.",
            file=sys.stderr,
        )
        return 1

    release_section = [f"## [{next_version}] - {release_date}", ""]
    release_section.extend(unreleased_body if unreleased_body else ["### Added", "- *(nothing yet)*"])
    release_section.extend(["", "---", ""])

    rebuilt_lines = []
    rebuilt_lines.extend(lines[:unreleased_idx])
    rebuilt_lines.extend(UNRELEASED_TEMPLATE)
    rebuilt_lines.extend(release_section)
    rebuilt_lines.extend(lines[first_release_idx:])
    rebuilt_lines = update_link_refs(rebuilt_lines, next_version)

    new_text = "\n".join(rebuilt_lines).rstrip() + "\n"

    if args.dry_run:
        print(f"Dry run: would cut version {next_version} on {release_date} in {changelog_path}")
        return 0

    changelog_path.write_text(new_text, encoding="utf-8")
    print(f"Updated {changelog_path}: cut {next_version} from Unreleased")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
