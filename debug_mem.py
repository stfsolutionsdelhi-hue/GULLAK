import json

with open('members_final.json') as f:
    members = json.load(f)

print(members[0])
