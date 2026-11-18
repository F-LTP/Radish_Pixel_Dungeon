# -*- coding: utf-8 -*-
"""次要护甲文本占位符自检：effect_desc 的 %d 个数 必须等于 effectArgs() 返回的元素个数。"""
import io, os, re, glob

ROOT = r'core/src/main'
PROPS = os.path.join(ROOT, 'assets/messages/items/items_zh.properties')
PKG = os.path.join(ROOT, 'java/com/shatteredpixel/shatteredpixeldungeon/items/armor/secondary')

# 1) 读 effect_desc
props = io.open(PROPS, encoding='utf-8').read().splitlines()
eff = {}
for line in props:
    m = re.match(r'items\.armor\.secondary\.([a-z0-9]+)\.effect_desc=(.*)$', line)
    if m:
        eff[m.group(1)] = m.group(2)

def count_ph(s):
    # %d 与 %d%% 都算一个参数；%% 不计
    s2 = s.replace('%d%%', '%d')
    s2 = s2.replace('%%', '')
    return len(re.findall(r'%d', s2)), len(re.findall(r'%s', s2))

print('== effect_desc 占位符 ==')
mismatch = []
for cls, text in sorted(eff.items()):
    n_int, n_str = count_ph(text)
    print('%-18s %d int / %d str' % (cls, n_int, n_str))

# 2) 读 effectArgs()
print('\n== effectArgs() 元素数 ==')
for path in sorted(glob.glob(os.path.join(PKG, '*.java'))):
    src = io.open(path, encoding='utf-8').read()
    name = os.path.basename(path)[:-5]
    m = re.search(r'effectArgs\s*\(\)\s*\{(.*?)\n\t\}', src, re.S)
    if not m:
        print('%-20s (no effectArgs override)' % name)
        continue
    body = m.group(1)
    if 'new Object[0]' in body or re.search(r'return\s+new\s+Object\s*\[\s*\]', body):
        n = 0
    else:
        inner = re.search(r'new\s+Object\s*\[\s*\]\s*\{(.*?)\}', body, re.S)
        if inner:
            n = len([x for x in inner.group(1).split(',') if x.strip()])
        else:
            n = -1
    key = name.lower()
    exp = count_ph(eff.get(key, ''))[0] if key in eff else -1
    flag = 'OK' if n == exp else '  <<< MISMATCH'
    print('%-20s args=%d  desc=%s%s' % (name, n, exp, flag))
    if n != exp:
        mismatch.append((name, n, exp))

print('\nMISMATCH:', mismatch if mismatch else 'none')
