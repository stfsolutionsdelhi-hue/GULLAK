import json
import re

# Complete structured data parser for all 415 receipt entries from Image 2
# Columns: Date | Ledger No | Particular | Cash/Card | Mobile | Receipt Amount | Narration | RD | LOAN RETURN | INTEREST

# Load members
with open('members_final.json', 'r') as f:
    members = json.load(f)

# Load payments
with open('payments_parsed.json', 'r') as f:
    payments = json.load(f)

print(f"Total Members loaded: {len(members)}")
print(f"Total Payments (Loans) loaded: {len(payments)}")
