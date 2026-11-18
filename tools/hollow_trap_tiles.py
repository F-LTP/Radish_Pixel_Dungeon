# -*- coding: utf-8 -*-
"""把陷阱图集里「已消耗/空心」那三行（行 3~5）换成新图（行 0~2）的掏空版。

terrain_features.png 是 256x128、每格 16x16；左边 9 列 x 7 行是陷阱区：
行 0~2 = 新换上去的实心图案，行 3~5 = 旧的空心（已触发）图案。
做法：取行 0~2 每格，只保留外轮廓、中间挖空，写回行 3~5 对应格。
"""
from PIL import Image, ImageFilter
import shutil, os, tempfile

SRC = r'core/src/main/assets/environment/terrain_features.png'
BAK = SRC + '.bak'
TILE = 16
COLS = 9
SRC_ROWS = [0, 1, 2]
DST_ROWS = [3, 4, 5]

if not os.path.exists(BAK):
    shutil.copy2(SRC, BAK)

im = Image.open(BAK).convert('RGBA')
W, H = im.size
assert (W, H) == (256, 128), im.size
px = im.load()


def hollow(tile):
    """只留 2px 外轮廓，中间全部透明。"""
    a = tile.getchannel('A')
    inner = a
    for _ in range(2):
        inner = inner.filter(ImageFilter.MinFilter(3))   # 3x3 腐蚀
    out = tile.copy()
    op, ip = out.load(), inner.load()
    for y in range(TILE):
        for x in range(TILE):
            if ip[x, y] > 0:      # 腐蚀后仍不透明的 = 内部，挖掉
                op[x, y] = (0, 0, 0, 0)
    return out


changed = 0
for sr, dr in zip(SRC_ROWS, DST_ROWS):
    for c in range(COLS):
        box = (c * TILE, sr * TILE, (c + 1) * TILE, (sr + 1) * TILE)
        tile = im.crop(box)
        if tile.getchannel('A').getextrema()[1] == 0:
            continue                     # 空格子跳过
        h = hollow(tile)
        im.paste(h, (c * TILE, dr * TILE))
        changed += 1

im.save(SRC)
print('hollow tiles written:', changed)

# 出对照图方便肉眼检查
prev = Image.new('RGBA', (COLS * TILE * 2 + 8, 6 * TILE), (30, 30, 36, 255))
before = Image.open(BAK).convert('RGBA')
for i, rows in enumerate(([0, 1, 2], [3, 4, 5])):
    for ri, r in enumerate(rows):
        prev.alpha_composite(before.crop((0, r * TILE, COLS * TILE, (r + 1) * TILE)), (0, (ri + 3 * i) * TILE))
for i, rows in enumerate(([0, 1, 2], [3, 4, 5])):
    for ri, r in enumerate(rows):
        prev.alpha_composite(im.crop((0, r * TILE, COLS * TILE, (r + 1) * TILE)),
                             (COLS * TILE + 8, (ri + 3 * i) * TILE))
prev.convert('RGB').resize((prev.width * 4, prev.height * 4), Image.NEAREST).save(
    os.path.join(tempfile.gettempdir(), 'trap_compare.png'))
print('compare written')
