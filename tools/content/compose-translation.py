"""Builds en.md / ru.md of a lesson from uz.md code blocks plus a prose template.

usage: compose.py <lesson dir> <lang>
  reads <lesson dir>/uz.md, <lesson dir>/<lang>.tpl.md and <lesson dir>/<lang>.map.json
  writes <lesson dir>/<lang>.md

The template holds the translated prose with placeholders @@CODE1@@, @@CODE2@@, ... — each is
replaced by the n-th group of consecutive python/java/cpp fences from uz.md (the whole group,
fences included). The map is {"uz text": "translated text"}, applied as plain substring
replacements inside those code groups only (comments and user-facing string literals).
"""
import io
import json
import re
import sys

folder, lang = sys.argv[1], sys.argv[2]
uz = io.open(f"{folder}/uz.md", encoding="utf-8").read()
tpl = io.open(f"{folder}/{lang}.tpl.md", encoding="utf-8").read()
mapping = json.load(io.open(f"{folder}/{lang}.map.json", encoding="utf-8"))

lines = uz.split("\n")
groups = []
i = 0
while i < len(lines):
    if re.match(r"^```(python|java|cpp)\s*$", lines[i]):
        start = i
        while True:
            j = i + 1
            while lines[j] != "```":
                j += 1
            i = j + 1
            if i < len(lines) and re.match(r"^```(python|java|cpp)\s*$", lines[i]):
                continue
            break
        groups.append("\n".join(lines[start:i]))
    else:
        i += 1

# longest keys first so that a short key never breaks a longer one
keys = sorted(mapping, key=len, reverse=True)
unused = set(keys)
out_groups = []
for g in groups:
    for k in keys:
        if k in g:
            g = g.replace(k, mapping[k])
            unused.discard(k)
    out_groups.append(g)

def put(match):
    n = int(match.group(1))
    return out_groups[n - 1]

result = re.sub(r"@@CODE(\d+)@@", put, tpl)
left = re.findall(r"[ʻʼ]|\b(?:boʻl|uchun|yoʻq|qilib|bilan)\b", "\n".join(out_groups))
io.open(f"{folder}/{lang}.md", "w", encoding="utf-8", newline="\n").write(result)
print(f"{lang}: {len(groups)} code groups, {len(unused)} unused keys", sorted(unused) if unused else "")
if left:
    print("possible untranslated Uzbek in code:", sorted(set(left)))
