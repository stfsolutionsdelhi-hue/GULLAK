import re

with open("Part2_Html.gs", "r", encoding="utf-8") as f:
    p2 = f.read()
with open("Part3A.gs", "r", encoding="utf-8") as f:
    p3a = f.read()
with open("Part3B.gs", "r", encoding="utf-8") as f:
    p3b = f.read()

ids_in_js = set(re.findall(r"document\.getElementById\([\"']([a-zA-Z0-9_-]+)[\"']\)", p3a + p3b))
print("Total unique IDs accessed in JS:", len(ids_in_js))

missing_ids = []
for elem_id in sorted(ids_in_js):
    pattern = r'id=["\']' + re.escape(elem_id) + r'["\']'
    if not re.search(pattern, p2):
        missing_ids.append(elem_id)

print(f"Missing IDs in Part2_Html.gs ({len(missing_ids)}):")
for m in missing_ids:
    print("  -", m)
