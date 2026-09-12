import re

with open("Part2_Html.gs", "r", encoding="utf-8") as f:
    p2 = f.read()
with open("Part3A.gs", "r", encoding="utf-8") as f:
    p3a = f.read()
with open("Part3B.gs", "r", encoding="utf-8") as f:
    p3b = f.read()

# Get all IDs in p2
ids_in_html = set(re.findall(r'id=["\']([a-zA-Z0-9_-]+)["\']', p2))

# Get all getElementById in JS
ids_in_js = set(re.findall(r'document\.getElementById\(["\']([a-zA-Z0-9_-]+)["\']\)', p3a + p3b))

print("Total IDs in HTML:", len(ids_in_html))
print("Total IDs accessed in JS:", len(ids_in_js))

missing_in_html = ids_in_js - ids_in_html
print("\n=== IDs in JS but NOT in HTML ===")
for m in sorted(missing_in_html):
    print("  -", m)
