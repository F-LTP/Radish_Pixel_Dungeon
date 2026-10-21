#!/usr/bin/env python3
"""Derive the damage-text colour of each floating-text icon.

The game tints a damage number with the dominant fill colour of the icon that
sits next to it (see DamagePalette). This script reads effects/text_icons.png,
splits it into the same 7x7 tiles the runtime uses, and reports the most common
opaque colour of each tile, ignoring pure black (the icons are drawn with a
black outline that would otherwise win every tile).

The values are meant to be copied into DamagePalette.java by hand, so the
runtime never touches the atlas to pick a colour. Re-run this script whenever
text_icons.png changes and update DamagePalette accordingly.

Run from the repository root:
    python3 tools/damage_icon_colors.py
    python3 tools/damage_icon_colors.py --java   # emit the Java constants
"""

from __future__ import annotations

import argparse
from collections import Counter
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:
    raise SystemExit(
        "Pillow is required. Install it with: python3 -m pip install Pillow"
    ) from exc

DEFAULT_IMAGE = Path("core/src/main/assets/effects/text_icons.png")

# Tile size, matching FloatingText.ICON_SIZE and its TextureFilm.
ICON_SIZE = 7

# Icon indices used for damage numbers (FloatingText constants). Positive,
# currency and level-up icons are deliberately excluded: they are not damage.
DAMAGE_ICONS = {
    0: "PHYS_DMG",
    1: "PHYS_DMG_NO_BLOCK",
    2: "MAGIC_DMG",
    3: "PICK_DMG",
    4: "HOLY",
    5: "HUNGER",
    6: "BURNING",
    7: "SHOCKING",
    8: "FROST",
    9: "WATER",
    10: "BLEEDING",
    11: "TOXIC",
    12: "CORROSION",
    13: "POISON",
    14: "OOZE",
    15: "DEFERRED",
    16: "CORRUPTION",
    17: "AMULET",
    25: "CRIT",
    26: "CRIT_NO_BLOCK",
}

# Non-damage icons, listed only so the report names every non-empty tile.
OTHER_ICONS = {
    18: "HEALING",
    19: "SHIELDING",
    20: "EXPERIENCE",
    21: "STRENGTH",
    23: "GOLD",
    24: "ENERGY",
    27: "EX_EXP",
    28: "BELIEF",
}


def dominant_color(image: Image.Image, index: int) -> int | None:
    """Most frequent opaque, non-black RGB packed as 0xRRGGBB, or None."""
    cols = image.width // ICON_SIZE
    row, col = divmod(index, cols)
    box = (col * ICON_SIZE, row * ICON_SIZE, col * ICON_SIZE + ICON_SIZE, row * ICON_SIZE + ICON_SIZE)
    tile = image.crop(box)

    counts: Counter[tuple[int, int, int]] = Counter()
    for count, (r, g, b, a) in tile.getcolors(ICON_SIZE * ICON_SIZE):
        if a == 0:
            continue
        if r == 0 and g == 0 and b == 0:
            continue
        counts[(r, g, b)] += count

    if not counts:
        return None
    (r, g, b), _ = counts.most_common(1)[0]
    return (r << 16) | (g << 8) | b


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--image", type=Path, default=DEFAULT_IMAGE, help=f"icon atlas (default: {DEFAULT_IMAGE})")
    parser.add_argument("--java", action="store_true", help="print the DamagePalette Java constants")
    args = parser.parse_args()

    image = Image.open(args.image).convert("RGBA")
    cols = image.width // ICON_SIZE
    total = (image.width // ICON_SIZE) * (image.height // ICON_SIZE)

    if args.java:
        print("    // 由 tools/damage_icon_colors.py 从 effects/text_icons.png 提取（各图标出现频率最高的非黑色）")
        for index in sorted(DAMAGE_ICONS):
            color = dominant_color(image, index)
            if color is None:
                raise SystemExit(f"icon {index} ({DAMAGE_ICONS[index]}) has no opaque non-black pixels")
            print(f"    public static final int {DAMAGE_ICONS[index]} = 0x{color:06x};")
        return

    print(f"{args.image}: {image.width}x{image.height}, {cols} columns, {total} tiles of {ICON_SIZE}x{ICON_SIZE}")
    print()
    print(f"{'idx':>3}  {'color':>8}  {'name':<18} kind")
    for index in sorted(set(DAMAGE_ICONS) | set(OTHER_ICONS)):
        color = dominant_color(image, index)
        if color is None:
            continue
        name = DAMAGE_ICONS.get(index) or OTHER_ICONS[index]
        kind = "damage" if index in DAMAGE_ICONS else "other"
        print(f"{index:>3}  0x{color:06x}  {name:<18} {kind}")

    empty = [i for i in range(total) if dominant_color(image, i) is None]
    print()
    print(f"empty tiles: {empty}")


if __name__ == "__main__":
    main()
