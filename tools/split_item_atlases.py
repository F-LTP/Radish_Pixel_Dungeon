#!/usr/bin/env python3
"""Split the legacy item and item-icon atlases into named PNG files."""

import argparse
import re
from pathlib import Path

from PIL import Image


DECLARATION = re.compile(
    r"(?:private|public)\s+static\s+final\s+int\s+"
    r"([A-Z_][A-Z0-9_]*)\s*=\s*([^;]+);"
)
XY_CALL = re.compile(r"xy\(\s*(\d+)\s*,\s*(\d+)\s*\)")
TOKEN = re.compile(r"[A-Z_][A-Z0-9_]*|\d+")


def strip_comments(source):
    source = re.sub(r"/\*.*?\*/", "", source, flags=re.DOTALL)
    return re.sub(r"//[^\n]*", "", source)


def class_body(source, class_name):
    match = re.search(r"\bclass\s+" + re.escape(class_name) + r"\b[^\{]*\{", source)
    if not match:
        raise ValueError("Class not found: " + class_name)

    start = match.end()
    depth = 1
    for index in range(start, len(source)):
        char = source[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return source[start:index], match.start(), index + 1
    raise ValueError("Unclosed class: " + class_name)


def parse_width(scope, default):
    match = re.search(r"\bWIDTH\s*=\s*(\d+)\s*;", scope)
    return int(match.group(1)) if match else default


def evaluate(expression, values, width):
    xy = XY_CALL.fullmatch(expression.strip())
    if xy:
        x, y = map(int, xy.groups())
        return (x - 1) + width * (y - 1)

    compact = re.sub(r"\s+", "", expression)
    tokens = TOKEN.findall(compact)
    if "+".join(tokens) != compact:
        return None

    result = 0
    for token in tokens:
        if token.isdigit():
            result += int(token)
        elif token in values:
            result += values[token]
        else:
            return None
    return result


def parse_constants(scope, default_width):
    width = parse_width(scope, default_width)
    values = {}
    resources = {}

    for match in DECLARATION.finditer(scope):
        name, expression = match.groups()
        value = evaluate(expression, values, width)
        if value is None:
            continue
        values[name] = value
        if match.group(0).lstrip().startswith("public") and name not in {"SIZE", "WIDTH"}:
            if name in resources:
                raise ValueError("Duplicate resource constant: " + name)
            resources[name] = value

    if not resources:
        raise ValueError("No resource constants found")
    return width, resources


def parse_item_sprite_sheet(java_path):
    source = strip_comments(java_path.read_text(encoding="utf-8"))
    outer, _, _ = class_body(source, "ItemSpriteSheet")
    icons, icons_start, icons_end = class_body(outer, "Icons")
    outer_without_icons = outer[:icons_start] + outer[icons_end:]

    item_width, items = parse_constants(outer_without_icons, 32)
    icon_width, icons = parse_constants(icons, 16)
    return (item_width, items), (icon_width, icons)


def export_frames(atlas_path, output_dir, frame_size, columns, resources):
    output_dir.mkdir(parents=True, exist_ok=True)
    written = set()

    with Image.open(atlas_path) as atlas:
        atlas.load()
        for name, frame_id in sorted(resources.items(), key=lambda pair: (pair[1], pair[0])):
            x = (frame_id % columns) * frame_size
            y = (frame_id // columns) * frame_size
            if x + frame_size > atlas.width or y + frame_size > atlas.height:
                raise ValueError(
                    f"{name} frame {frame_id} lies outside {atlas_path} "
                    f"at ({x}, {y})"
                )
            filename = name.lower() + ".png"
            atlas.crop((x, y, x + frame_size, y + frame_size)).save(output_dir / filename)
            written.add(filename)
    return written


def remove_misclassified(output_dir, valid_names, other_names):
    removed = []
    for filename in sorted(other_names - valid_names):
        path = output_dir / filename
        if path.exists():
            path.unlink()
            removed.append(filename)
    return removed


def write_manifest(output_dir):
    filenames = sorted(path.name for path in output_dir.glob("*.png"))
    (output_dir / "manifest.txt").write_text("\n".join(filenames) + "\n", encoding="utf-8")
    return len(filenames)


def main():
    project = Path(__file__).resolve().parent.parent
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java", type=Path, required=True, help="Legacy ItemSpriteSheet.java")
    parser.add_argument("--items-atlas", type=Path, required=True, help="Legacy 16x16 items.png")
    parser.add_argument(
        "--icons-atlas",
        type=Path,
        default=project / "core/src/main/assets/sprites/item_icons.png",
        help="Legacy 8x8 item_icons.png",
    )
    parser.add_argument(
        "--items-output",
        type=Path,
        default=project / "core/src/main/assets/sprites/items",
    )
    parser.add_argument(
        "--icons-output",
        type=Path,
        default=project / "core/src/main/assets/sprites/item_icons",
    )
    parser.add_argument(
        "--clean-misclassified",
        action="store_true",
        help="Remove icon-only files that the old splitter placed in the item directory",
    )
    args = parser.parse_args()

    (item_width, items), (icon_width, icons) = parse_item_sprite_sheet(args.java)
    item_files = export_frames(args.items_atlas, args.items_output, 16, item_width, items)
    icon_files = export_frames(args.icons_atlas, args.icons_output, 8, icon_width, icons)

    removed = []
    if args.clean_misclassified:
        removed = remove_misclassified(args.items_output, item_files, icon_files)

    item_manifest_count = write_manifest(args.items_output)
    icon_manifest_count = write_manifest(args.icons_output)
    print(f"Exported {len(item_files)} item frames ({item_manifest_count} total item PNGs)")
    print(f"Exported {len(icon_files)} icon frames ({icon_manifest_count} total icon PNGs)")
    print(f"Removed {len(removed)} misclassified item PNGs")


if __name__ == "__main__":
    main()
