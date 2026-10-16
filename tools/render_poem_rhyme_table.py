#!/usr/bin/env python3
"""Render the Poem rhyme table JSON into a readable PNG.

Each final gets its own row, with equipment chips on the left and monster
chips on the right. Non-Chinese fallback entries are summarized in a footer.

Usage:
    python3 tools/render_poem_rhyme_table.py
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw, ImageFont

DEFAULT_INPUT = Path("core/src/main/assets/rhyme/poem_rhyme_table_zh.json")
DEFAULT_OUTPUT = Path("core/src/main/assets/rhyme/poem_rhyme_table_zh.png")
FONT_PATH = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"

BG = (18, 20, 26)
PANEL = (28, 31, 38)
PANEL_ALT = (24, 27, 34)
GRID = (60, 68, 82)
TEXT = (236, 240, 246)
MUTED = (158, 170, 190)
EQUIP_BG = (42, 72, 112)
EQUIP_BORDER = (86, 131, 187)
MOB_BG = (106, 70, 33)
MOB_BORDER = (190, 133, 74)
EMPTY_BG = (52, 56, 66)
EMPTY_BORDER = (110, 116, 126)
ACCENT = (120, 180, 255)

TITLE_SIZE = 42
SUBTITLE_SIZE = 22
HEADER_SIZE = 24
ROW_LABEL_SIZE = 30
CHIP_SIZE = 22
FOOTER_SIZE = 20

MARGIN_X = 40
MARGIN_Y = 36
TITLE_H = 70
SUBTITLE_H = 42
HEADER_H = 42
ROW_PAD_Y = 12
ROW_PAD_X = 16
CHIP_PAD_X = 14
CHIP_PAD_Y = 7
CHIP_GAP_X = 10
CHIP_GAP_Y = 10
COLUMN_GAP = 24
LABEL_COL_W = 150
FOOTER_H = 84


def font(size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(FONT_PATH, size)


def text_size(draw: ImageDraw.ImageDraw, text: str, fnt: ImageFont.FreeTypeFont) -> tuple[int, int]:
    box = draw.textbbox((0, 0), text, font=fnt)
    return int(box[2] - box[0]), int(box[3] - box[1])


def chip_size(draw: ImageDraw.ImageDraw, text: str, fnt: ImageFont.FreeTypeFont) -> tuple[int, int]:
    w, h = text_size(draw, text, fnt)
    return w + CHIP_PAD_X * 2, h + CHIP_PAD_Y * 2


def layout_chips(draw: ImageDraw.ImageDraw, names: Iterable[str], col_width: int, fnt: ImageFont.FreeTypeFont):
    lines: list[list[tuple[str, int, int]]] = []
    current: list[tuple[str, int, int]] = []
    current_w = 0
    line_h = 0
    for name in names:
        cw, ch = chip_size(draw, name, fnt)
        needed = cw if not current else current_w + CHIP_GAP_X + cw
        if current and needed > col_width:
            lines.append(current)
            current = []
            current_w = 0
            line_h = 0
        current.append((name, cw, ch))
        current_w = cw if len(current) == 1 else current_w + CHIP_GAP_X + cw
        line_h = max(line_h, ch)
    if current:
        lines.append(current)

    heights = [max(ch for _, _, ch in line) for line in lines] if lines else []
    total_h = sum(heights) + CHIP_GAP_Y * max(0, len(lines) - 1)
    return lines, total_h


def draw_chip(draw: ImageDraw.ImageDraw, xy, text, fnt, bg, border, text_color=TEXT):
    x, y, w, h = xy
    r = h // 2
    draw.rounded_rectangle([x, y, x + w, y + h], radius=r, fill=bg, outline=border, width=2)
    tw, th = text_size(draw, text, fnt)
    tx = x + (w - tw) / 2
    ty = y + (h - th) / 2 - 1
    draw.text((tx, ty), text, font=fnt, fill=text_color)


def draw_section(draw: ImageDraw.ImageDraw, x, y, width, title, items, fnt_title, fnt_chip, bg, border, empty_label):
    draw.text((x, y), title, font=fnt_title, fill=MUTED)
    y += 28
    if not items:
        tw, th = text_size(draw, empty_label, fnt_chip)
        pad_w = tw + CHIP_PAD_X * 2
        pad_h = th + CHIP_PAD_Y * 2
        draw_chip(draw, (x, y, pad_w, pad_h), empty_label, fnt_chip, EMPTY_BG, EMPTY_BORDER, MUTED)
        return pad_h

    lines, total_h = layout_chips(draw, items, width, fnt_chip)
    cy = y
    for line in lines:
        line_h = max(ch for _, _, ch in line)
        cx = x
        for name, cw, ch in line:
            draw_chip(draw, (cx, cy, cw, ch), name, fnt_chip, bg, border)
            cx += cw + CHIP_GAP_X
        cy += line_h + CHIP_GAP_Y
    return 28 + total_h


def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args(argv)

    obj = json.loads(args.input.read_text(encoding="utf-8"))
    groups = obj.get("groups") or obj.get("rhyme_groups") or []
    non_chinese = obj.get("unmatched") or obj.get("non_chinese", {})

    # Prepare a temporary drawing surface for measurements.
    measure = Image.new("RGBA", (10, 10))
    draw = ImageDraw.Draw(measure)
    title_font = font(TITLE_SIZE)
    subtitle_font = font(SUBTITLE_SIZE)
    header_font = font(HEADER_SIZE)
    row_label_font = font(ROW_LABEL_SIZE)
    chip_font = font(CHIP_SIZE)
    footer_font = font(FOOTER_SIZE)

    content_w = 3200 - MARGIN_X * 2
    row_w = content_w
    label_w = LABEL_COL_W
    equip_w = (row_w - label_w - COLUMN_GAP) // 2
    mob_w = row_w - label_w - COLUMN_GAP - equip_w

    row_heights = []
    per_row_layout = []
    for group in groups:
        eq_lines, eq_h = layout_chips(draw, [e["name"] for e in group["equipment"]], equip_w, chip_font)
        mob_lines, mob_h = layout_chips(draw, [m["name"] for m in group["monsters"]], mob_w, chip_font)
        row_h = max(eq_h, mob_h, 36) + ROW_PAD_Y * 2 + 32
        row_heights.append(row_h)
        per_row_layout.append((group, eq_lines, mob_lines, row_h))

    total_h = MARGIN_Y + TITLE_H + SUBTITLE_H + HEADER_H + sum(row_heights) + FOOTER_H + (len(groups) - 1) * 2
    img = Image.new("RGBA", (3200, total_h), BG)
    d = ImageDraw.Draw(img)

    # Title block
    d.text((MARGIN_X, MARGIN_Y), "月华·诗 押韵表", font=title_font, fill=TEXT)
    d.text((MARGIN_X, MARGIN_Y + 54), "中文：按最后一个汉字的韵母分组；非中文：回退到首字符匹配", font=subtitle_font, fill=MUTED)
    counts = obj.get('counts', {})
    group_count = counts.get('groups', counts.get('rhyme_groups', len(groups)))
    non_chinese_monsters = counts.get('unmatched_monsters', counts.get('non_chinese_monsters', len(non_chinese.get('monsters', []))))
    d.text((MARGIN_X, MARGIN_Y + 84), f"装备 {counts.get('equipment', 0)}  ·  怪物 {counts.get('monsters', 0)}  ·  组 {group_count}  ·  非中文怪物 {non_chinese_monsters}", font=subtitle_font, fill=MUTED)

    # Header
    y = MARGIN_Y + TITLE_H + SUBTITLE_H
    d.rounded_rectangle([MARGIN_X, y, 3200 - MARGIN_X, y + HEADER_H], radius=12, fill=PANEL, outline=GRID, width=2)
    d.text((MARGIN_X + 18, y + 8), "final", font=header_font, fill=ACCENT)
    d.text((MARGIN_X + label_w + 10, y + 8), "装备", font=header_font, fill=ACCENT)
    d.text((MARGIN_X + label_w + equip_w + COLUMN_GAP + 10, y + 8), "怪物", font=header_font, fill=ACCENT)
    y += HEADER_H + 12

    # Rows
    for idx, (group, eq_lines, mob_lines, row_h) in enumerate(per_row_layout):
        fill = PANEL if idx % 2 == 0 else PANEL_ALT
        d.rounded_rectangle([MARGIN_X, y, 3200 - MARGIN_X, y + row_h], radius=16, fill=fill, outline=GRID, width=2)
        cy = y + 16
        final = group["final"] or group.get("bucket") or group.get("initial") or ""
        d.text((MARGIN_X + 26, cy + 4), final, font=row_label_font, fill=TEXT)
        d.text((MARGIN_X + 20, cy + 42), f"{len(group['equipment'])}/{len(group['monsters'])}", font=subtitle_font, fill=MUTED)

        # Equipment column
        ex = MARGIN_X + label_w + 10
        mx = ex + equip_w + COLUMN_GAP
        ey = cy
        my = cy
        for line in eq_lines:
            line_h = max(ch for _, _, ch in line)
            cx = ex
            for name, cw, ch in line:
                draw_chip(d, (cx, ey, cw, ch), name, chip_font, EQUIP_BG, EQUIP_BORDER)
                cx += cw + CHIP_GAP_X
            ey += line_h + CHIP_GAP_Y
        if not group['equipment']:
            draw_chip(d, (ex, ey, 84, 34), "无", chip_font, EMPTY_BG, EMPTY_BORDER, MUTED)

        # Monster column
        for line in mob_lines:
            line_h = max(ch for _, _, ch in line)
            cx = mx
            for name, cw, ch in line:
                draw_chip(d, (cx, my, cw, ch), name, chip_font, MOB_BG, MOB_BORDER)
                cx += cw + CHIP_GAP_X
            my += line_h + CHIP_GAP_Y
        if not group['monsters']:
            draw_chip(d, (mx, my, 84, 34), "无", chip_font, EMPTY_BG, EMPTY_BORDER, MUTED)

        y += row_h + 2

    # Footer
    footer_y = total_h - FOOTER_H + 10
    d.line((MARGIN_X, footer_y - 8, 3200 - MARGIN_X, footer_y - 8), fill=GRID, width=2)
    note = f"非中文怪物 {len(non_chinese.get('monsters', []))} 个，游戏内按首字符回退匹配；此图仅展示中文 final 分组。"
    d.text((MARGIN_X, footer_y), note, font=footer_font, fill=MUTED)
    d.text((MARGIN_X, footer_y + 28), f"数据源：{args.input}", font=footer_font, fill=MUTED)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    img.save(args.output)
    print(f"wrote {args.output}")
    print(f"size={img.size[0]}x{img.size[1]}")


if __name__ == "__main__":
    main()
