#!/usr/bin/env python3
"""Generate the Chinese rhyme table used by Moonlight's Poem toy.

Only Chinese needs a maintained table because Han characters do not contain
explicit pronunciation data. Other locales should read localized item/monster
names through game code (Messages.java) and compare their first letter/digit at
runtime.

The script reads Chinese message bundles, extracts the last Chinese character
of each gameplay equipment and monster name, computes its pinyin final via
pypinyin, and writes a compact JSON map:

    { "<final>": ["字", "字", ...], ... }

Run from the repository root:
    python3 tools/generate_poem_rhyme_table.py
"""

from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from pathlib import Path
from typing import Iterable

try:
    from pypinyin import Style, lazy_pinyin
except ImportError as exc:
    raise SystemExit(
        "pypinyin is required. Install it with: python3 -m pip install pypinyin"
    ) from exc

ITEMS_ZH = Path("core/src/main/assets/messages/items/items_zh.properties")
ACTORS_ZH = Path("core/src/main/assets/messages/actors/actors_zh.properties")
DEFAULT_OUTPUT = Path("core/src/main/assets/poem_rhyme_table_zh.json")

CJK_RE = re.compile(r"[\u4e00-\u9fff]")
FORMAT_RE = re.compile(r"%\d*\$?[sdif]|%%")


def decode_properties_value(value: str) -> str:
    value = value.rstrip("\n\r")
    out: list[str] = []
    i = 0
    while i < len(value):
        ch = value[i]
        if ch != "\\" or i + 1 >= len(value):
            out.append(ch)
            i += 1
            continue
        nxt = value[i + 1]
        if nxt == "u" and i + 5 < len(value):
            hex_part = value[i + 2 : i + 6]
            try:
                out.append(chr(int(hex_part, 16)))
                i += 6
                continue
            except ValueError:
                pass
        escapes = {"t": "\t", "n": "\n", "r": "\r", "f": "\f"}
        out.append(escapes.get(nxt, nxt))
        i += 2
    return "".join(out).strip()


def parse_properties(path: Path) -> dict[str, str]:
    entries: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or line.startswith("!"):
            continue
        sep_positions = [pos for pos in (line.find("="), line.find(":")) if pos != -1]
        if not sep_positions:
            continue
        sep = min(sep_positions)
        key = line[:sep].strip()
        entries[key] = decode_properties_value(line[sep + 1 :])
    return entries


def clean_name(name: str) -> str:
    name = FORMAT_RE.sub("", name)
    return name.replace("_", "").replace("\ufeff", "").strip()


def last_chinese_char(text: str) -> str | None:
    matches = CJK_RE.findall(text)
    return matches[-1] if matches else None


def chinese_final(ch: str) -> str | None:
    finals = lazy_pinyin(ch, style=Style.FINALS, strict=False, errors="ignore")
    return finals[0] if finals else None


def include_equipment_key(key: str, include_inner: bool) -> bool:
    if not key.endswith(".name"):
        return False
    if "$" in key and not include_inner:
        return False
    if key.startswith("items.weapon.curses.") or key.startswith("items.weapon.enchantments."):
        return False
    if key.startswith("items.armor.curses.") or key.startswith("items.armor.glyphs."):
        return False
    return key.startswith("items.weapon.") or key.startswith("items.armor.")


def include_monster_key(key: str, include_npcs: bool, include_inner: bool) -> bool:
    if not key.startswith("actors.mobs.") or not key.endswith(".name"):
        return False
    if "$" in key and not include_inner:
        return False
    if key.startswith("actors.mobs.npcs.") and not include_npcs:
        return False
    return True


def last_chars(entries: Iterable[tuple[str, str]], include_npcs: bool, include_inner: bool) -> dict[str, set[str]]:
    """Return {final: {char, ...}} from message bundle entries."""
    by_final: dict[str, set[str]] = defaultdict(set)
    for key, value in entries:
        name = clean_name(value)
        ch = last_chinese_char(name)
        if ch is None:
            continue
        final = chinese_final(ch)
        if final is None:
            continue
        by_final[final].add(ch)
    return by_final


def build_table(include_npcs: bool, include_inner: bool) -> dict[str, list[str]]:
    items = parse_properties(ITEMS_ZH)
    actors = parse_properties(ACTORS_ZH)

    eq_chars = last_chars(
        ((k, v) for k, v in items.items() if include_equipment_key(k, include_inner)),
        include_npcs, include_inner,
    )
    mon_chars = last_chars(
        ((k, v) for k, v in actors.items() if include_monster_key(k, include_npcs, include_inner)),
        include_npcs, include_inner,
    )

    # merge all finals
    all_finals: set[str] = set(eq_chars) | set(mon_chars)
    table: dict[str, list[str]] = {}
    for final in sorted(all_finals):
        chars: set[str] = set()
        chars.update(eq_chars.get(final, set()))
        chars.update(mon_chars.get(final, set()))
        table[final] = sorted(chars)
    return table


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--include-npcs", action="store_true", help="include actors.mobs.npcs.* names")
    parser.add_argument("--include-inner", action="store_true", help="include keys containing '$'")
    parser.add_argument("--compact", action="store_true", help="write compact JSON")
    args = parser.parse_args(argv)

    table = build_table(args.include_npcs, args.include_inner)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    indent = None if args.compact else 2
    args.output.write_text(json.dumps(table, ensure_ascii=False, indent=indent) + "\n", encoding="utf-8")

    char_count = sum(len(v) for v in table.values())
    print(f"wrote {args.output}")
    print(f"groups={len(table)} chars={char_count}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
