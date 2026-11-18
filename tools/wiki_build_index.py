# -*- coding: utf-8 -*-
"""扫描仓库文档，生成 docs/wiki/INDEX.md。

用法：python tools/wiki_build_index.py
输出：docs/wiki/INDEX.md（全量清单：按目录分组，带一级标题与行数）
"""
import io
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "docs", "wiki", "INDEX.md")

GROUPS = [
    ("docs/wiki", "本 Wiki"),
    ("docs/萝卜地牢开发文档", "开发专题"),
    ("docs/萝卜地牢进度文档", "进度 / 待办 / 日志"),
    ("docs", "仓库其它文档"),
]


def first_heading(path):
    try:
        for line in io.open(path, encoding="utf-8", errors="ignore"):
            line = line.strip()
            if line.startswith("# "):
                return line[2:].strip()
    except OSError:
        pass
    return ""


def count_lines(path):
    try:
        with io.open(path, encoding="utf-8", errors="ignore") as f:
            return sum(1 for _ in f)
    except OSError:
        return 0


def collect(rel_dir):
    base = os.path.join(ROOT, rel_dir.replace("/", os.sep))
    found = []
    if not os.path.isdir(base):
        return found
    for dirpath, _dirnames, filenames in os.walk(base):
        for name in sorted(filenames):
            if not name.lower().endswith(".md"):
                continue
            if name == "INDEX.md":
                continue
            full = os.path.join(dirpath, name)
            rel = os.path.relpath(full, ROOT).replace(os.sep, "/")
            # 只收当前分组根目录下的文件，子目录仍收录但标注完整相对路径
            found.append((rel, first_heading(full), count_lines(full)))
    return sorted(found, key=lambda x: x[0])


lines = ["# 文档索引", "", "> 由 `python tools/wiki_build_index.py` 自动生成，不要手改。", ""]

seen = set()
for rel_dir, title in GROUPS:
    items = collect(rel_dir)
    items = [it for it in items if it[0] not in seen]
    if not items:
        continue
    for it in items:
        seen.add(it[0])
    lines.append("## " + title)
    lines.append("")
    lines.append("| 文件 | 标题 | 行数 |")
    lines.append("|---|---|---|")
    for rel, heading, n in items:
        # wiki 内的链接用相对路径，其余用仓库相对路径
        if rel.startswith("docs/wiki/"):
            link = os.path.basename(rel)
        else:
            link = os.path.relpath(os.path.join(ROOT, rel.replace("/", os.sep)),
                                   os.path.join(ROOT, "docs", "wiki")).replace(os.sep, "/")
        lines.append("| [%s](%s) | %s | %d |" % (os.path.basename(rel), link, heading or "—", n))
    lines.append("")

io.open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
print("wrote", OUT, "entries:", len(seen))
